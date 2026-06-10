package com.aurorabank.core.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurorabank.core.domain.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);
}
