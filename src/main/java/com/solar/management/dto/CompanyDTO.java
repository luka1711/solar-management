package com.solar.management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class CompanyDTO {
    @NotNull(message = "Company name can not be null.")
    private String name;
    @NotNull(message = "Company address can not be null.")
    private String address;
    @NotNull(message = "Company phone can not be null.")
    private String phone;
}
