package com.solar.management.repository;

import com.solar.management.model.Customer;
import org.springframework.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmailAndFullNameAndCompanyId(String email, String fullName, Long companyId);

    Optional<Customer> findById(@NonNull Long id);
    // Base queries by scope
    List<Customer> findAllByOrderByFullNameAsc();                             // ALL
    List<Customer> findByCompanyIdOrderByFullNameAsc(Long companyId);         // COMPANY
    List<Customer> findByProjects_AssignedInstaller_IdOrderByFullNameAsc(Long installerId); // INSTALLER

    // Filter by name
    List<Customer> findByFullNameOrderByFullNameAsc(String name);
    List<Customer> findByFullNameAndCompanyIdOrderByFullNameAsc(String name, Long companyId);
    List<Customer> findByFullNameAndProjects_AssignedInstaller_IdOrderByFullNameAsc(String name, Long installerId);

    // Filter by email
    List<Customer> findByEmailOrderByFullNameAsc(String email);
    List<Customer> findByEmailAndCompanyIdOrderByFullNameAsc(String email, Long companyId);
    List<Customer> findByEmailAndProjects_AssignedInstaller_IdOrderByFullNameAsc(String email, Long installerId);


}
