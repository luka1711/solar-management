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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> addCustomer(Authentication authentication, @RequestBody @Valid CustomerDTO customerDTO) throws CustomerException {
        String userInitiatedEmail = authentication.getName();
        return customerService.createCustomer(customerDTO, userInitiatedEmail);
    }

    @GetMapping()
    public ResponseEntity<?> getCustomers(Authentication authentication,
    @RequestParam(required = false) String name, @RequestParam(required = false) String email,
                                          @RequestParam(required = false) String companyName
    ) throws CustomerException {
        String userInitiatedEmail = authentication.getName();
        return customerService.getCustomers(name, email, companyName, userInitiatedEmail);
    }
}
