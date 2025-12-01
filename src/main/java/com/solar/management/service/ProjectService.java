package com.solar.management.service;

import com.solar.management.dto.ProjectDTO;
import com.solar.management.dto.ProjectStatusDTO;
import com.solar.management.exception.ProjectException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ProjectService {

    ResponseEntity<?> createProject(ProjectDTO projectDTO, String userInitiated) throws ProjectException;
    ResponseEntity<?> getProjects(String userInitiated) throws ProjectException;
    public ResponseEntity<?> getProjectById(String userInitiated, Long projectId) throws ProjectException;
    ResponseEntity<?> updateProjectStatus(String userInitiated, Long projectId, ProjectStatusDTO projectStatus) throws ProjectException;

}
