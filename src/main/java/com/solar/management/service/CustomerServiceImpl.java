package com.solar.management.service;

import com.solar.management.dto.ApiResponse;
import com.solar.management.dto.CustomerDTO;
import com.solar.management.dto.CustomerResponseDTO;
import com.solar.management.exception.CustomerException;
import com.solar.management.model.*;
import com.solar.management.repository.CompanyRepository;
import com.solar.management.repository.CustomerRepository;
import com.solar.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, UserRepository userRepository, CompanyRepository companyRepository) {
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public ResponseEntity<?> createCustomer(CustomerDTO customerDTO, String userInitiatedEmail) throws CustomerException {
        User user = userRepository.findByEmail(userInitiatedEmail)
                .orElseThrow(() -> new CustomerException("User initiated is not found."));

        if (user.getRole() == Role.SUPER_ADMIN && customerDTO.getCompanyName() == null){
            throw new CustomerException("You need to provide a company name.");
        }

        Company company = companyRepository.findByName(customerDTO.getCompanyName())
                .orElseThrow(() -> new CustomerException("Provided company does not exist"));

        if (Boolean.TRUE.equals(customerRepository.existsByEmailAndFullNameAndCompanyId(customerDTO.getEmail(), customerDTO.getFullName(), company.getId())))
            throw new CustomerException("Company already exists");


        Customer customer = Customer.builder()
                .email(customerDTO.getEmail())
                .fullName(customerDTO.getFullName())
                .address(customerDTO.getAddress())
                .phone(customerDTO.getPhone())
                .company(company)
                .build();

        customerRepository.save(customer);

        CustomerResponseDTO customerResponseDTO = CustomerResponseDTO.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .fullName(customer.getFullName())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .companyName(company.getName())
                .build();
        ApiResponse<CustomerResponseDTO> apiResponse = new ApiResponse<>("ok", customerResponseDTO);

        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> getCustomers(String name, String email, String companyName, String userInitiatedEmail) throws CustomerException {

        validateFilters(name, email);

        User requester = userRepository.findByEmail(userInitiatedEmail)
                .orElseThrow(() -> new CustomerException("User not found."));

        AccessScope scope = resolveScope(requester);

        List<Customer> customers;

        if (name != null) {
            customers = getCustomersByNameAndScope(name, scope, requester);
        } else if (email != null) {
            customers = getCustomersByEmailAndScope(email, scope, requester);
        } else if (companyName != null) {
            customers = getCustomersByCompanyNameAndScope(companyName, scope, requester);
        } else {
            customers = getCustomersByScope(scope, requester);
        }

        List<CustomerResponseDTO> dtoList = customers.stream()
                .map(CustomerResponseDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(new ApiResponse<>("ok", dtoList));

    }


    void validateFilters(String name, String email) throws CustomerException {
        if (name != null && email != null) {
            throw new CustomerException("Provide only one of: name or email.");
        }
    }

    public AccessScope resolveScope(User user) {
        return switch (user.getRole()) {
            case SUPER_ADMIN -> AccessScope.ALL;
            case COMPANY_ADMIN, ENGINEER -> AccessScope.COMPANY;
            case INSTALLER -> AccessScope.INSTALLER;
        };
    }

    private List<Customer> getCustomersByScope(AccessScope scope, User requester) {
        return switch (scope) {
            case ALL -> customerRepository.findAllByOrderByFullNameAsc();
            case COMPANY -> customerRepository.findByCompanyIdOrderByFullNameAsc(requester.getCompany().getId());
            case INSTALLER -> customerRepository.findByProjects_AssignedInstaller_IdOrderByFullNameAsc(requester.getId());
        };
    }

    private List<Customer> getCustomersByNameAndScope(String name, AccessScope scope, User requester) {
        return switch (scope) {
            case ALL -> customerRepository.findByFullNameOrderByFullNameAsc(name);
            case COMPANY -> customerRepository.findByFullNameAndCompanyIdOrderByFullNameAsc(name, requester.getCompany().getId());
            case INSTALLER -> customerRepository.findByFullNameAndProjects_AssignedInstaller_IdOrderByFullNameAsc(name, requester.getId());
        };
    }

    private List<Customer> getCustomersByEmailAndScope(String email, AccessScope scope, User requester) {
        return switch (scope) {
            case ALL -> customerRepository.findByEmailOrderByFullNameAsc(email);
            case COMPANY -> customerRepository.findByEmailAndCompanyIdOrderByFullNameAsc(email, requester.getCompany().getId());
            case INSTALLER -> customerRepository.findByEmailAndProjects_AssignedInstaller_IdOrderByFullNameAsc(email, requester.getId());
        };
    }

    private List<Customer> getCustomersByCompanyNameAndScope(String companyName, AccessScope scope, User requester)
            throws CustomerException {

        Company company = companyRepository.findByName(companyName)
                .orElseThrow(() -> new CustomerException("Company not found."));

        if ((scope == AccessScope.COMPANY) && !company.getId().equals(requester.getCompany().getId())) {
            throw new CustomerException("Not allowed to view customers of another company.");
        }
        if(scope == AccessScope.INSTALLER) {
            throw new CustomerException("Not allowed to view all the company customer's");
        }

        return customerRepository.findByCompanyIdOrderByFullNameAsc(company.getId());
    }



}
