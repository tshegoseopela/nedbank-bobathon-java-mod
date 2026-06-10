package com.aurorabank.core.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurorabank.core.domain.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
}
