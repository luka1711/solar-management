package com.solar.management.service;

import com.solar.management.dto.ApiResponse;
import com.solar.management.dto.CustomerDTO;
import com.solar.management.dto.CustomerResponseDTO;
import com.solar.management.exception.CustomerException;
import com.solar.management.model.Company;
import com.solar.management.model.Customer;
import com.solar.management.model.Role;
import com.solar.management.model.User;
import com.solar.management.repository.CompanyRepository;
import com.solar.management.repository.CustomerRepository;
import com.solar.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    public ResponseEntity<?> getCustomers(String userInitiatedEmail) throws CustomerException {
        return new ResponseEntity<>(customerRepository.findAll(), HttpStatus.OK);
    }
}
