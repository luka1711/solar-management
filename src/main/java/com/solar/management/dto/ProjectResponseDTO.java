package com.solar.management.dto;

import com.solar.management.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponseDTO {
    private Long id;

    private Double roofArea;
    private Double roofAngle;
    private String panelType;
    private Integer panelWatt;
    private String inverterModel;
    private Double expectedProductionKwhYr;
    private Double projectCost;
    private CustomerDTO projectCustomer;
    private UserDTO assignedInstaller;

    private String address;
    private Double latitude;
    private Double longitude;

    private Double estimatedDailyKwh;
    private Double estimatedMonthlyKwh;
    private Integer totalNumberOfPanels;


}
