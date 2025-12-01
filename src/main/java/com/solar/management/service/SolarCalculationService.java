package com.solar.management.service;

import com.solar.management.dto.ApiResponse;
import com.solar.management.dto.ProjectCalculationRequestDTO;
import com.solar.management.dto.ProjectProductionDTO;
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

        double panelDensity = request.getPanelWatt() / panelArea;

        double utilizationFactor = 0.65; // 65% efektivnog korišćenja površine

        double peakPower = request.getRoofArea() * utilizationFactor * panelDensity / 1000.0; // kWp
        //optional -- will think if we should return it
        int numberOfPanels = (int) (request.getRoofArea() * utilizationFactor / panelArea);


        String url = UriComponentsBuilder.fromUriString("https://re.jrc.ec.europa.eu/api/pvcalc")
                .queryParam("lat", request.getLatitude())
                .queryParam("lon", request.getLongitude())
                .queryParam("angle", request.getRoofAngle())
                .queryParam("loss", request.getSystemLoss())
                .queryParam("aspect", 0)
                .queryParam("outputformat", "json")
                .queryParam("pvcalculation", "1")
                .queryParam("mountingplace", "building")
                .queryParam("peakpower", peakPower)// kWp
                 .toUriString();
        System.out.println("url: "+ url);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if(response == null || !response.containsKey("outputs")) { throw new RuntimeException("PVGIS API error"); }

        Map<String, Object> outputs = (Map<String, Object>) response.get("outputs");

        if (!outputs.containsKey("totals")) {
            throw new RuntimeException("PVGIS API output missing 'totals' section.");
        }
        Map<String, Object> totals = (Map<String, Object>) outputs.get("totals");

        if (!totals.containsKey("fixed")) {
            throw new RuntimeException("PVGIS API output missing 'fixed' section under 'totals'.");
        }

        Map<String, Object> fixed = (Map<String, Object>) totals.get("fixed");

        if (!fixed.containsKey("E_y")) {
            throw new RuntimeException("PVGIS API output missing 'E_y' (Yearly Energy).");
        }

        double expectedProductionKwhYr = ((Number) fixed.get("E_y")).doubleValue();

        double estimatedDailyKwh = expectedProductionKwhYr / 365.0;
        double estimatedMonthlyKwh = expectedProductionKwhYr / 12.0;

        ProjectProductionDTO projectCalculation = ProjectProductionDTO.builder()
                .expectedProductionKwhYr(expectedProductionKwhYr)
                .estimatedDailyKwh(estimatedDailyKwh)
                .estimatedMonthlyKwh(estimatedMonthlyKwh)
                .build();

        project.setEstimatedDailyKwh(estimatedDailyKwh);
        project.setEstimatedMonthlyKwh(estimatedMonthlyKwh);
        project.setExpectedProductionKwhYr(expectedProductionKwhYr);


        ApiResponse<ProjectProductionDTO> apiResponse = new ApiResponse<>("ok", projectCalculation);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

}
