package com.solar.management.service;

import com.solar.management.dto.ApiResponse;
import com.solar.management.dto.ProjectCalculationRequestDTO;
import com.solar.management.dto.ProjectProductionDTO;
import com.solar.management.dto.RoofSectionDTO;
import com.solar.management.exception.ProjectException;
import com.solar.management.model.Project;
import com.solar.management.model.User;
import com.solar.management.repository.ProjectRepository;
import com.solar.management.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class SolarCalculationService {
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectServiceImpl projectService;

    public SolarCalculationService(RestTemplateBuilder builder, UserRepository userRepository, ProjectRepository projectRepository, ProjectServiceImpl projectService) {
        this.restTemplate = builder.build();
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }

    @Transactional
    public ResponseEntity<?> calculate(String userInitiated, Long projectId, ProjectCalculationRequestDTO request) throws ProjectException {
        User requester = userRepository.findByEmail(userInitiated)
                .orElseThrow(() -> new ProjectException("User initiated is not found."));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException("Project with id: " + projectId + " doesn't exist"));

        if(!projectService.validateProjectAccess(requester, project)){
            throw new ProjectException("User does not have access to this project");
        }

        double panelArea = 2.0;
        double panelDensity = request.getPanelWatt() / panelArea; // W/m2
        double utilizationFactor = 0.65;

        double totalExpectedProductionKwhYr = 0.0;
        //not used for now. Will include
        double totalPeakPower = 0.0;
        int totalNumberOfPanels = 0;

        if (request.getSections() == null || request.getSections().isEmpty()) {
            throw new ProjectException("Project calculation request must contain at least one roof section.");
        }

        for (RoofSectionDTO section : request.getSections()) {

            double peakPowerSection = section.getRoofArea() * utilizationFactor * panelDensity / 1000.0; // kWp

            int numberOfPanelsSection = (int) (section.getRoofArea() * utilizationFactor / panelArea);

            double energyKwhYrSection = getAnnualEnergy(
                    request.getLatitude(),
                    request.getLongitude(),
                    section.getRoofAngle(),
                    request.getSystemLoss(),
                    section.getAspect(),
                    peakPowerSection
            );

            totalExpectedProductionKwhYr += energyKwhYrSection;
            totalPeakPower += peakPowerSection;
            totalNumberOfPanels += numberOfPanelsSection;
        }

        double estimatedDailyKwh = totalExpectedProductionKwhYr / 365.0;
        double estimatedMonthlyKwh = totalExpectedProductionKwhYr / 12.0;

        ProjectProductionDTO projectCalculation = ProjectProductionDTO.builder()
                .expectedProductionKwhYr(totalExpectedProductionKwhYr)
                .estimatedDailyKwh(estimatedDailyKwh)
                .estimatedMonthlyKwh(estimatedMonthlyKwh)
                .totalNumberOfPanels(totalNumberOfPanels)
                .totalPeakPower(totalPeakPower)
                .build();

        project.setEstimatedDailyKwh(estimatedDailyKwh);
        project.setEstimatedMonthlyKwh(estimatedMonthlyKwh);
        project.setExpectedProductionKwhYr(totalExpectedProductionKwhYr);
        project.setTotalNumberOfPanels(totalNumberOfPanels);
        project.setTotalPeakPower(totalPeakPower);


        ApiResponse<ProjectProductionDTO> apiResponse = new ApiResponse<>("ok", projectCalculation);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    private double getAnnualEnergy(double lat, double lon, double angle, double loss, int aspect, double peakPower) {
        // 1. Kreiranje URL-a
        String url = UriComponentsBuilder.fromUriString("https://re.jrc.ec.europa.eu/api/pvcalc")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("angle", angle)
                .queryParam("loss", loss)
                .queryParam("aspect", aspect)
                .queryParam("outputformat", "json")
                .queryParam("pvcalculation", "1")
                .queryParam("mountingplace", "building")
                .queryParam("peakpower", peakPower)
                .toUriString();

        // 2. Poziv API-ja
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if(response == null || !response.containsKey("outputs")) {
            throw new RuntimeException("PVGIS API error: No outputs returned.");
        }

        // 3. Parsiranje E_y (kWh)
        Map<String, Object> outputs = (Map<String, Object>) response.get("outputs");
        Map<String, Object> totals = (Map<String, Object>) outputs.get("totals");
        Map<String, Object> fixed = (Map<String, Object>) totals.get("fixed");

        // E_y je Godišnja Proizvodnja u kWh (za pvcalc)
        return ((Number) fixed.get("E_y")).doubleValue();
    }

}
