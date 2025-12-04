package com.solar.management.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
@Builder
@Getter
public class RoofSectionDTO {
    private double roofArea;
    private double roofAngle;
    private int aspect;
}
