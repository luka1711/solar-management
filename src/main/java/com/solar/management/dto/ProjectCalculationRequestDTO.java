package com.solar.management.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class ProjectCalculationRequestDTO {
    private double latitude;
    private double longitude;
    private double roofAngle;
    private double roofArea;
    private double panelWatt;
    private double systemLoss;
}
