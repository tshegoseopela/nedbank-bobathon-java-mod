package com.aurorabank.core.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurorabank.core.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
