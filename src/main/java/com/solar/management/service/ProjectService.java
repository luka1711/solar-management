package com.solar.management.service;

import com.solar.management.dto.ProjectDTO;
import com.solar.management.exception.ProjectException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface ProjectService {

    ResponseEntity<?> createProject(ProjectDTO projectDTO, String userInitiated) throws ProjectException;


}
