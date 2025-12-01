package com.solar.management.controller;

import com.solar.management.dto.CompanyDTO;
import com.solar.management.dto.RegistrationDTO;
import com.solar.management.exception.AuthenticationException;
import com.solar.management.exception.CompanyException;
import com.solar.management.service.CompanyService;
import com.solar.management.service.CompanyServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private final CompanyServiceImpl companyService;

    @Autowired
    public AdminController(CompanyServiceImpl companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/api/admin/company")
    public ResponseEntity<?> createCompany(@RequestBody @Valid CompanyDTO dto) throws CompanyException {

        String userInitiatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return companyService.createCompany(dto, userInitiatedEmail);
    }
}
