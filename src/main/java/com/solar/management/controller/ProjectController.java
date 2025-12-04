package com.solar.management.controller;

import com.solar.management.dto.ProjectCalculationRequestDTO;
import com.solar.management.dto.ProjectDTO;
import com.solar.management.dto.ProjectStatusDTO;
import com.solar.management.exception.ProjectException;
import com.solar.management.service.ProjectServiceImpl;
import com.solar.management.service.SolarCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectServiceImpl projectService;
    private final SolarCalculationService solarCalculationService;
    @Autowired
    public ProjectController(ProjectServiceImpl projectService, SolarCalculationService solarCalculationService) {
        this.projectService = projectService;
        this.solarCalculationService = solarCalculationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPER_ADMIN', 'ENGINEER')")
    public ResponseEntity<?> createProject(@RequestBody ProjectDTO project, Authentication authentication) throws ProjectException {
        String userInitiated = authentication.getName();

        return projectService.createProject(project, userInitiated);
    }

    @GetMapping
    public ResponseEntity<?> getAllProjects(Authentication authentication) throws ProjectException {
        String userInitiated = authentication.getName();

        return projectService.getProjects(userInitiated);
    }

    @PutMapping("/{projectId}/status")
    public ResponseEntity<?> updateProjectStatus(@RequestBody ProjectStatusDTO projectStatusDTO, @PathVariable Long projectId, Authentication authentication) throws ProjectException {
        String userInitiated = authentication.getName();

        return projectService.updateProjectStatus(userInitiated, projectId, projectStatusDTO);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProjectById(@PathVariable Long projectId, Authentication authentication) throws ProjectException {
        String userInitiated = authentication.getName();

        return projectService.getProjectById(userInitiated, projectId);
    }

    @PutMapping("/{projectId}/installer/{installerId}")
    public ResponseEntity<?> assignInstaller(@PathVariable Long projectId, @PathVariable Long installerId, Authentication authentication) throws ProjectException {
        String userInitiated = authentication.getName();

        return projectService.assignInstaller(userInitiated, projectId, installerId);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable Long projectId, Authentication authentication) throws ProjectException {
        String userInitiated = authentication.getName();

        return projectService.deleteProject(userInitiated, projectId);
    }

    @PostMapping("/{projectId}/calculation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<?> performSolarCalculations(@RequestBody ProjectCalculationRequestDTO projectCalculationRequestDTO, @PathVariable Long projectId, Authentication authentication) throws ProjectException {

        String userInitiated = authentication.getName();

        return solarCalculationService.calculate(userInitiated, projectId, projectCalculationRequestDTO);
    }
}
