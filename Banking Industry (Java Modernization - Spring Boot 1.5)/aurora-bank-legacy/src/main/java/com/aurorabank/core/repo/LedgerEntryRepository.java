package com.aurorabank.core.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aurorabank.core.domain.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByAccountIdOrderByPostedAtDesc(Long accountId);
}
