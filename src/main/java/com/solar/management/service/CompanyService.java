package com.solar.management.service;

import com.solar.management.dto.CompanyDTO;
import com.solar.management.exception.CompanyException;
import com.solar.management.model.Company;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface CompanyService {
    public ResponseEntity<?> createCompany(CompanyDTO company, String userInitiatedEmail) throws CompanyException;

}
