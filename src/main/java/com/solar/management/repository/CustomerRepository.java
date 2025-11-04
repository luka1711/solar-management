package com.solar.management.repository;

import com.solar.management.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmailAndFullNameAndCompanyId(String email, String fullName, Long companyId);
}
