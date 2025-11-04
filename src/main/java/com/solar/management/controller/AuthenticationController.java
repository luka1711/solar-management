package com.solar.management.controller;

import com.solar.management.dto.LoginDTO;
import com.solar.management.dto.RegistrationDTO;
import com.solar.management.exception.AuthenticationException;
import com.solar.management.service.AuthenticationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
public class AuthenticationController {

    private final AuthenticationServiceImpl authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationServiceImpl authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/api/admin/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegistrationDTO dto) throws AuthenticationException {

        String creatorEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return authenticationService.registerUser(dto, creatorEmail);
    }

    @PostMapping("api/auth/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginDTO user) throws AuthenticationException {
        return authenticationService.loginUser(user);
    }
}
