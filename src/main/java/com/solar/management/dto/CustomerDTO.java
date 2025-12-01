package com.solar.management.dto;

import com.solar.management.model.Customer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CustomerDTO {

    @NotNull(message = "Name of a user should be provided")
    private String fullName;
    @NotNull(message = "Address should be provided.")
    @NotBlank(message = "Address should be provided.")
    private String address;
    @NotNull(message = "Phone should be provided.")
    @NotBlank(message = "Phone should be provided.")
    private String phone;
    @NotNull(message = "Email should be provided.")
    @NotBlank(message = "Email should be provided.")
    private String email;

    private String companyName;

    public static CustomerDTO from(Customer c) {
        if (c == null) return null;
        return CustomerDTO.builder()
                .fullName(c.getFullName())
                .address(c.getAddress())
                .phone(c.getPhone())
                .email(c.getEmail())
                .companyName(c.getCompany() != null ? c.getCompany().getName() : null)
                .build();
    }
}
