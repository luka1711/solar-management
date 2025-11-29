package com.solar.management.repository;

import com.solar.management.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Boolean existsByCustomerIdAndAddressAndCompanyId(Long customerId, String address, Long companyId);
    List<Project> findByCompanyId(Long companyId);
    List<Project> findByAssignedInstallerIdAndCompanyId(Long assignedInstallerId, Long companyId);
}
