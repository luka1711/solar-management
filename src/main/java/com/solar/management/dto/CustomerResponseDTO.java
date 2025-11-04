package com.solar.management.dto;

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
}
