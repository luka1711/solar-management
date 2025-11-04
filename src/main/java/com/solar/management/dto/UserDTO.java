package com.solar.management.dto;

import com.solar.management.model.Company;
import com.solar.management.model.Role;
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
}
