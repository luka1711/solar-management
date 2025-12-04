package com.solar.management.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Builder
@Getter
public class ProjectCalculationRequestDTO {
    private double latitude;
    private double longitude;
    private double panelWatt;
    private double systemLoss;
    private List<RoofSectionDTO> sections;
}
