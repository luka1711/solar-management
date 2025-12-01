package com.solar.management.service;

import com.solar.management.dto.LoginDTO;
import com.solar.management.dto.RegistrationDTO;
import org.springframework.http.ResponseEntity;
import com.solar.management.exception.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public interface AuthenticationService {

    public ResponseEntity<?> registerUser(RegistrationDTO user, String userInitiatedEmail) throws AuthenticationException;
    public ResponseEntity<?> loginUser(LoginDTO user) throws AuthenticationException;
}
