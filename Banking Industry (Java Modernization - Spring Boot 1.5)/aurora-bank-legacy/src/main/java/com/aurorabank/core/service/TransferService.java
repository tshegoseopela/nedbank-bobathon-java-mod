package com.aurorabank.core.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurorabank.core.domain.Account;
import com.aurorabank.core.domain.LedgerEntry;
import com.aurorabank.core.domain.Transfer;
import com.aurorabank.core.repo.AccountRepository;
import com.aurorabank.core.repo.LedgerEntryRepository;
import com.aurorabank.core.repo.TransferRepository;

@Service
public class TransferService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private TransferRepository transferRepository;

    // Moves funds from one account to another and writes the double-entry
    // ledger postings. Note: there is no @Transactional boundary here.
    public Transfer transfer(Long fromAccountId, Long toAccountId, double amount, String memo) {
        if (amount <= 0) {
            throw new RuntimeException("Amount must be positive.");
        }

        Account from = accountRepository.findOne(fromAccountId);
        Account to = accountRepository.findOne(toAccountId);

        if (from == null || to == null) {
            throw new RuntimeException("Account not found.");
        }
        if (!"ACTIVE".equals(from.getStatus()) || !"ACTIVE".equals(to.getStatus())) {
            throw new RuntimeException("Both accounts must be ACTIVE.");
        }

        // Read-check-write on the balance with no locking.
        if (from.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds.");
        }

        from.setBalance(from.getBalance() - amount);
        accountRepository.save(from);

        to.setBalance(to.getBalance() + amount);
        accountRepository.save(to);

        LedgerEntry debit = new LedgerEntry();
        debit.setAccountId(fromAccountId);
        debit.setEntryType("DEBIT");
        debit.setAmount(amount);
        debit.setDescription(memo);
        debit.setPostedAt(new Date());
        ledgerEntryRepository.save(debit);

        LedgerEntry credit = new LedgerEntry();
        credit.setAccountId(toAccountId);
        credit.setEntryType("CREDIT");
        credit.setAmount(amount);
        credit.setDescription(memo);
        credit.setPostedAt(new Date());
        ledgerEntryRepository.save(credit);

        Transfer transfer = new Transfer();
        transfer.setReference("TRF-" + System.currentTimeMillis());
        transfer.setFromAccountId(fromAccountId);
        transfer.setToAccountId(toAccountId);
        transfer.setAmount(amount);
        transfer.setStatus("POSTED");
        transfer.setCreatedAt(new Date());
        return transferRepository.save(transfer);
    }
}
