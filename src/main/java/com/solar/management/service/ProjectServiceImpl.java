package com.solar.management.service;

import com.solar.management.dto.ApiResponse;
import com.solar.management.dto.CustomerResponseDTO;
import com.solar.management.dto.ProjectDTO;
import com.solar.management.exception.CustomerException;
import com.solar.management.exception.ProjectException;
import com.solar.management.model.*;
import com.solar.management.repository.CompanyRepository;
import com.solar.management.repository.CustomerRepository;
import com.solar.management.repository.ProjectRepository;
import com.solar.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, CustomerRepository customerRepository, UserRepository userRepository,
                              CompanyRepository companyRepository) {
        this.projectRepository = projectRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public ResponseEntity<?> createProject(ProjectDTO projectDTO, String userInitiated) throws ProjectException {
        Company company;
        Customer customer;
        if(!customerRepository.existsById(projectDTO.getCustomerId())){
            throw new ProjectException("Customer with id "+ projectDTO.getCustomerId() +" does not exist");
        }
        else customer = customerRepository.findById(projectDTO.getCustomerId())
                .orElseThrow(() -> new ProjectException("Customer is not found"));

        User user = userRepository.findByEmail(userInitiated)
                .orElseThrow(() -> new ProjectException("User initiated is not found."));

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
                .status(ProjectStatus.CREATED)
                .build();

        projectRepository.save(project);

        ApiResponse<String> apiResponse = new ApiResponse<>("ok", "Project created successfully.");
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

}

