package com.solar.management.dto;

import com.solar.management.model.Company;
import com.solar.management.model.Customer;
import com.solar.management.model.Role;
import com.solar.management.model.User;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class UserDTO {
    private String email;
    private Role role;
    private String companyName;
    private String userInitiated;

    public static UserDTO from(User c) {
        if (c == null) return null;
        return UserDTO.builder()
                .email(c.getEmail())
                .role(c.getRole())
                .companyName(c.getCompany().getName())
                .userInitiated(c.getUserInitiated())
                .build();
    }
}
