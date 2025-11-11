package com.solar.management.repository;

import com.solar.management.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Boolean existsByCustomerIdAndAddressAndCompanyId(Long customerId, String address, Long companyId);

}
