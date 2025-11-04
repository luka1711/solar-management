package com.solar.management.controller;

import com.solar.management.dto.CustomerDTO;
import com.solar.management.exception.CustomerException;
import com.solar.management.service.CustomerService;
import com.solar.management.service.CustomerServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerServiceImpl customerService;
    @Autowired
    public CustomerController(CustomerServiceImpl customerService) {
        this.customerService = customerService;
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN','SUPER_ADMIN', 'ENGINEER')")
    public ResponseEntity<?> addCustomer(@RequestBody @Valid CustomerDTO customerDTO) throws CustomerException {
        String userInitiatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerService.createCustomer(customerDTO, userInitiatedEmail);
    }
}
