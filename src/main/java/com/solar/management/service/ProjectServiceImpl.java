package com.solar.management.service;

import com.solar.management.dto.*;
import com.solar.management.exception.CustomerException;
import com.solar.management.exception.ProjectException;
import com.solar.management.model.*;
import com.solar.management.repository.CompanyRepository;
import com.solar.management.repository.CustomerRepository;
import com.solar.management.repository.ProjectRepository;
import com.solar.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskSchedulingProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final TaskSchedulingProperties taskSchedulingProperties;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository,
                              CustomerRepository customerRepository,
                              UserRepository userRepository,
                              CompanyRepository companyRepository, TaskSchedulingProperties taskSchedulingProperties) {
        this.projectRepository = projectRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.taskSchedulingProperties = taskSchedulingProperties;
    }

    @Override
    public ResponseEntity<?> createProject(ProjectDTO projectDTO, String userInitiated) throws ProjectException {
        Company company;
        Customer customer;
        User user = userRepository.findByEmail(userInitiated)
                .orElseThrow(() -> new ProjectException("User initiated is not found."));

        if(!customerRepository.existsById(projectDTO.getCustomerId())){
            throw new ProjectException("Customer with id "+ projectDTO.getCustomerId() +" does not exist");
        }
        else customer = customerRepository.findById(projectDTO.getCustomerId())
                .orElseThrow(() -> new ProjectException("Customer is not found"));


        if (user.getRole() == Role.SUPER_ADMIN){
            if(projectDTO.getCompanyId() == null){
                throw new ProjectException("companyId required for SUPER_ADMIN");
            }
            company = companyRepository.findById(projectDTO.getCompanyId())
                    .orElseThrow(() -> new ProjectException("Company with id "+ projectDTO.getCompanyId() +" does not exist"));
        }
        else if (projectDTO.getCompanyId() != null){
            if (!projectDTO.getCompanyId().equals(user.getCompany().getId())){
                throw new ProjectException("Authenticated user can't create project for provided company.");
            }
            company = companyRepository.findById(projectDTO.getCompanyId())
                    .orElseThrow(() -> new ProjectException("Company with id "+ projectDTO.getCompanyId() +" does not exist"));
        }
        else company = companyRepository.findById(user.getCompany().getId())
                .orElseThrow(() -> new ProjectException("Company with id "+ user.getCompany().getId() +" does not exist"));

        if(Boolean.TRUE.equals(projectRepository.existsByCustomerIdAndAddressAndCompanyId(projectDTO.getCustomerId(), projectDTO.getAddress(), company.getId()))) {
            throw new ProjectException("Customer with the project at the same location already exists");
        }

        Project project  = Project.builder()
                .roofAngle(projectDTO.getRoofAngle())
                .roofArea(projectDTO.getRoofArea())
                .panelType(projectDTO.getPanelType())
                .panelWatt(projectDTO.getPanelWatt())
                .inverterModel(projectDTO.getInverterModel())
                .company(company)
                .customer(customer)
                .address(projectDTO.getAddress())
                .latitude(projectDTO.getLatitude())
                .longitude(projectDTO.getLongitude())
                .build();

        projectRepository.save(project);

        ApiResponse<String> apiResponse = new ApiResponse<>("ok", "Project created successfully.");
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> getProjects(String userInitiated) throws ProjectException {
        User requester = userRepository.findByEmail(userInitiated)
                .orElseThrow(() -> new ProjectException("User initiated is not found."));

        AccessScope scope = resolveScope(requester);

        List<Project> projectList = getProjectsByScope(scope, requester);

        List<ProjectResponseDTO> projectResponse = projectList.stream()
                .map(this::toDto)
                .toList();

        ApiResponse<List<ProjectResponseDTO>> apiResponse = new ApiResponse<>("ok", projectResponse);

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getProjectById(String userInitiated, Long projectId) throws ProjectException {
        User requester = userRepository.findByEmail(userInitiated)
                .orElseThrow(() -> new ProjectException("User initiated is not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException("Project with id: " + projectId + " doesn't exist"));

        if(validateProjectAccess(requester, project)){
            ProjectResponseDTO projectResponseDTO = toDto(project);

            ApiResponse<ProjectResponseDTO> apiResponse = new ApiResponse<>("ok", projectResponseDTO);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }
        else throw new ProjectException("User does not have access to the requested project");
    }

    @Override
    public ResponseEntity<?> updateProjectStatus(String userInitiated, Long projectId, ProjectStatusDTO projectStatus) throws ProjectException {
        User requester = userRepository.findByEmail(userInitiated)
                .orElseThrow(() -> new ProjectException("User initiated is not found."));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException("Project with id: " + projectId + " doesn't exist"));

        if(validateProjectAccess(requester, project)){
            if(requester.getRole() == Role.INSTALLER &&
                    !(projectStatus.getProjectStatus().equals(ProjectStatus.INSTALLING) || projectStatus.getProjectStatus().equals(ProjectStatus.TESTING))){
                throw new ProjectException("User is INSTALLER and doesn't have permission to do requested updates.");
            }
            else{
                project.setStatus(projectStatus.getProjectStatus());
                projectRepository.save(project);
                ApiResponse<String> apiResponse = new ApiResponse<>("ok", "Project updated successfully.");
                return new ResponseEntity<>(apiResponse, HttpStatus.OK);
            }
        }
        else throw new ProjectException("User doesn't have access to this project");
    }

    @Override
    public ResponseEntity<?> assignInstaller(String userInitiated, Long projectId, Long installerId) throws ProjectException {
        User requester = userRepository.findByEmail(userInitiated)
                .orElseThrow(() -> new ProjectException("User initiated is not found."));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException("Project with id: " + projectId + " doesn't exist"));

        if(validateProjectAccess(requester, project)){
            User installer = userRepository.findById(installerId)
                    .orElseThrow(() -> new ProjectException("Installer with id: " + installerId + " doesn't exist"));

            if(!installer.getCompany().getId().equals(project.getCompany().getId())){
                throw new ProjectException("Provided installer with id: " + installerId + " is not part of company's project");
            }
            else if(installer.getRole().equals(Role.INSTALLER)){
                project.setAssignedInstaller(installer);
                projectRepository.save(project);
                ApiResponse<String> apiResponse = new ApiResponse<>("ok", "Project assigned successfully");
                return new ResponseEntity<>(apiResponse, HttpStatus.OK);
            }
            else throw new ProjectException("Requested user with id: " + installerId +" is not installer");

        }
        else throw new ProjectException("User does not have access to the requested project");
    }

    @Override
    public ResponseEntity<?> deleteProject(String userInitiated, Long projectId) throws ProjectException {
        User requester = userRepository.findByEmail(userInitiated)
                .orElseThrow(() -> new ProjectException("User initiated is not found."));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException("Project with id: " + projectId + " doesn't exist"));

        if (validateProjectAccess(requester, project)){
            projectRepository.delete(project);
            ApiResponse<String> apiResponse = new ApiResponse<>("ok", "Project deleted successfully");
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } else throw new ProjectException("User does not have access to the requested project");
    }


    public AccessScope resolveScope(User user) {
        return switch (user.getRole()) {
            case SUPER_ADMIN -> AccessScope.ALL;
            case COMPANY_ADMIN, ENGINEER -> AccessScope.COMPANY;
            case INSTALLER -> AccessScope.INSTALLER;
        };
    }

    public List<Project> getProjectsByScope(AccessScope scope, User userInitiated){

        if(scope == AccessScope.ALL){
            return projectRepository.findAll();
        }
        else if (scope == AccessScope.COMPANY) {
            return projectRepository.findByCompanyId(userInitiated.getCompany().getId());
        }
        else{
            return projectRepository.findByAssignedInstallerIdAndCompanyId(userInitiated.getId(), userInitiated.getCompany().getId());
        }
    }

    //to map project entity to entity that will be used in response
    private ProjectResponseDTO toDto(Project project) {
        return ProjectResponseDTO.builder()
                .id(project.getId())
                .roofArea(project.getRoofArea())
                .roofAngle(project.getRoofAngle())
                .panelType(project.getPanelType())
                .panelWatt(project.getPanelWatt())
                .inverterModel(project.getInverterModel())
                .expectedProductionKwhYr(project.getExpectedProductionKwhYr())
                .projectCost(project.getProjectCost())
                .projectCustomer(CustomerDTO.from(project.getCustomer()))
                .assignedInstaller(UserDTO.from(project.getAssignedInstaller()))
                .address(project.getAddress())
                .latitude(project.getLatitude())
                .longitude(project.getLongitude())
                .estimatedDailyKwh(project.getEstimatedDailyKwh())
                .estimatedMonthlyKwh(project.getEstimatedMonthlyKwh())
                .build();
    }

    public boolean validateProjectAccess(User user, Project project) {
        if (user.getRole() == Role.SUPER_ADMIN) {
            return true;
        }
        else if (user.getRole() == Role.INSTALLER && !Objects.equals(project.getAssignedInstaller().getId(), user.getId())){
            return false;
        }
        else{
            return Objects.equals(user.getCompany().getId(), project.getCompany().getId());
        }
    }


}

