package com.imansdev.ackownt.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.imansdev.ackownt.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    Optional<Customer> findByNationalId(String nationalId);

}
