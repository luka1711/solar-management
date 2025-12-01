package com.solar.management.service;

import com.solar.management.dto.ApiResponse;
import com.solar.management.dto.CompanyDTO;
import com.solar.management.exception.CompanyException;
import com.solar.management.model.Company;
import com.solar.management.model.User;
import com.solar.management.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImpl implements CompanyService{

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public ResponseEntity<?> createCompany(CompanyDTO companyDTO, String userInitiatedEmail) throws CompanyException {

        if (Boolean.TRUE.equals(companyRepository.existsByName(companyDTO.getName()))) throw new CompanyException("Company already exists");

        Company company = Company.builder()
                .name(companyDTO.getName())
                .address(companyDTO.getAddress())
                .phone(companyDTO.getPhone())
                .userInitiated(userInitiatedEmail)
                .build();

        companyRepository.save(company);

        ApiResponse<Company> apiResponse = new ApiResponse<>("ok", company);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);

    }
}
