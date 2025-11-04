package com.solar.management.service;

import com.solar.management.dto.CustomerDTO;
import com.solar.management.exception.CustomerException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface CustomerService {

    public ResponseEntity<?> createCustomer(CustomerDTO customerDTO, String userInitiatedEmail) throws CustomerException;
    public ResponseEntity<?> getCustomers(String userInitiatedEmail) throws CustomerException;
}
