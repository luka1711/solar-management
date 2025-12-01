package com.solar.management.dto;

import com.solar.management.model.Customer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String address;
    private String phone;
    private String companyName;

    // Static factory to convert from Customer entity
    public static CustomerResponseDTO fromEntity(Customer customer) {
        return CustomerResponseDTO.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .address(customer.getAddress())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .companyName(customer.getCompany() != null ? customer.getCompany().getName() : null)
                .build();
    }
}
