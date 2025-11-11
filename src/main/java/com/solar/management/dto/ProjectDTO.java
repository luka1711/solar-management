package com.solar.management.dto;

import lombok.Data;

@Data
public class ProjectDTO {
    private Double roofArea;
    private Double roofAngle;
    private String panelType;
    private Integer panelWatt;
    private String inverterModel;
    private Long customerId;
    private Long companyId;
    private String address;
    private Double latitude;
    private Double longitude;
}
