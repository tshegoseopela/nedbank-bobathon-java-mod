package com.aurorabank.core.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aurorabank.core.domain.Account;
import com.aurorabank.core.domain.LedgerEntry;
import com.aurorabank.core.repo.AccountRepository;
import com.aurorabank.core.repo.LedgerEntryRepository;

@Service
public class InterestService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    // Annual interest rates by account type.
    private static final double SAVINGS_ANNUAL_RATE = 0.0325;
    private static final double CHECKING_ANNUAL_RATE = 0.0010;

    // Posts one month of interest to every ACTIVE account.
    // Interest is computed with floating-point arithmetic.
    public void postMonthlyInterest() {
        List<Account> accounts = accountRepository.findAll();

        for (Account account : accounts) {
            if (!"ACTIVE".equals(account.getStatus())) {
                continue;
            }

            double annualRate = "SAVINGS".equals(account.getAccountType())
                    ? SAVINGS_ANNUAL_RATE
                    : CHECKING_ANNUAL_RATE;

            double monthlyInterest = account.getBalance() * annualRate / 12.0;

            account.setBalance(account.getBalance() + monthlyInterest);
            accountRepository.save(account);

            LedgerEntry entry = new LedgerEntry();
            entry.setAccountId(account.getId());
            entry.setEntryType("CREDIT");
            entry.setAmount(monthlyInterest);
            entry.setDescription("Monthly interest");
            entry.setPostedAt(new Date());
            ledgerEntryRepository.save(entry);
        }
    }
}
