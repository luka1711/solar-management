package com.solar.management.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectProductionDTO {
    private double expectedProductionKwhYr;
    private double estimatedDailyKwh;
    private double estimatedMonthlyKwh;
}
