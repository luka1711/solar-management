package com.solar.management.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "project")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double roofArea;
    private Double roofAngle;
    private String panelType;
    private Integer panelWatt;
    private String inverterModel;
    private Double expectedProductionKwhYr;
    private Double projectCost;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status = ProjectStatus.CREATED;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Document> documents = List.of();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_installer_id")
    private User assignedInstaller;

    private String address;
    private Double latitude;
    private Double longitude;

    private Double estimatedDailyKwh;
    private Double estimatedMonthlyKwh;


}
