package com.solar.management.controller;

import com.solar.management.dto.ProjectDTO;
import com.solar.management.dto.ProjectStatusDTO;
import com.solar.management.exception.ProjectException;
import com.solar.management.service.ProjectServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectServiceImpl projectService;
    @Autowired
    public ProjectController(ProjectServiceImpl projectService) {
        this.projectService = projectService;
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
    public ResponseEntity<?> updateProject(@RequestBody ProjectStatusDTO projectStatusDTO, @PathVariable Long projectId, Authentication authentication) throws ProjectException {
        String userInitiated = authentication.getName();

        return projectService.updateProjectStatus(userInitiated, projectId, projectStatusDTO);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getProjectById(@PathVariable Long projectId, Authentication authentication) throws ProjectException {
        String userInitiated = authentication.getName();

        return projectService.getProjectById(userInitiated, projectId);
    }
}
