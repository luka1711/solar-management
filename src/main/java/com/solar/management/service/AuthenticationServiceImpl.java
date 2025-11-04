package com.solar.management.service;

import com.solar.management.dto.*;
import com.solar.management.exception.AuthenticationException;
import com.solar.management.model.Company;
import com.solar.management.model.Role;
import com.solar.management.model.User;
import com.solar.management.repository.CompanyRepository;
import com.solar.management.repository.UserRepository;
import com.solar.management.security.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenGenerator tokenGenerator;

    @Autowired
    public AuthenticationServiceImpl(UserRepository userRepository, CompanyRepository companyRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenGenerator = tokenGenerator;
    }
    @Override
    public ResponseEntity<?> registerUser(RegistrationDTO registrationDTO, String userInitiatedEmail) throws AuthenticationException {

        User userInitiated = userRepository.findByEmail(userInitiatedEmail)
                .orElseThrow(() -> new AuthenticationException("User initiated is not found."));

        if(userRepository.existsByEmail(registrationDTO.getEmail())) throw new AuthenticationException("Email already in use.");

        if (registrationDTO.getRole() == Role.SUPER_ADMIN &&
                userInitiated.getRole() != Role.SUPER_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to create a SUPER_ADMIN");
        }
        Company company;

        if(userInitiated.getRole() == Role.SUPER_ADMIN && registrationDTO.getRole() == Role.SUPER_ADMIN){
            if (registrationDTO.getCompanyName() != null) {
                throw new AuthenticationException("Company name should not be provided for super admin user.");
            }
            company = null;
        }
        else if (userInitiated.getRole() == Role.SUPER_ADMIN) {
            if (registrationDTO.getCompanyName() == null) {
                throw new AuthenticationException("companyName is required for " + registrationDTO.getRole());
            }
            company = companyRepository.findByName(registrationDTO.getCompanyName())
                    .orElseThrow(() -> new AuthenticationException("Company not found!"));
        }
        else {
            company = userInitiated.getCompany();
        }

        User newUser = User.builder()
                .email(registrationDTO.getEmail())
                .password(passwordEncoder.encode(registrationDTO.getPassword()))
                .role(registrationDTO.getRole())
                .company(company)
                .userInitiated(userInitiatedEmail)
                .build();

        userRepository.save(newUser);

        UserDTO userDTO = UserDTO.builder()
                .companyName(newUser.getCompany().getName())
                .email(newUser.getEmail())
                .role(newUser.getRole())
                .userInitiated(userInitiatedEmail)
                .build();
        ApiResponse<UserDTO> apiResponse = new ApiResponse<>("ok", userDTO);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> loginUser(LoginDTO user) throws AuthenticationException {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = tokenGenerator.generateToken(authentication);
            return new ResponseEntity<>(new AuthResponseDTO(token), HttpStatus.OK);
        }
        catch (BadCredentialsException e) {
            // Handle invalid username or password
            throw new AuthenticationException("Invalid username or password");
        } catch (DisabledException e) {
            // Handle account disabled
            throw new AuthenticationException("Account is disabled");
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Handle other authentication errors
            throw new AuthenticationException("Authentication failed");
        }
    }
}
