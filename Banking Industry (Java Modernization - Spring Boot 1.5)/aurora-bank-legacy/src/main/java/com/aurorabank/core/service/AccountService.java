package com.aurorabank.core.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.aurorabank.core.domain.Account;
import com.aurorabank.core.domain.Customer;
import com.aurorabank.core.repo.AccountRepository;
import com.aurorabank.core.repo.CustomerRepository;

@Service
public class AccountService {

    // Field injection (legacy style).
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DataSource dataSource;

    // Shared, non-thread-safe formatter used to build account numbers.
    private static final SimpleDateFormat ACCT_NO_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    public Account openAccount(Long customerId, String accountType, String currency) {
        Customer customer = customerRepository.findOne(customerId);
        if (customer == null) {
            throw new RuntimeException("Unknown customer: " + customerId);
        }

        Account account = new Account();
        account.setAccountNumber("ACC-" + ACCT_NO_FORMAT.format(new Date()));
        account.setCustomerId(customerId);
        account.setAccountType(accountType);
        account.setCurrency(currency);
        account.setBalance(0.0);
        account.setStatus("ACTIVE");
        account.setOpenedAt(new Date());

        return accountRepository.save(account);
    }

    public Account getById(Long accountId) {
        return accountRepository.findOne(accountId);
    }

    public List<Account> getForCustomer(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    // Charges a maintenance fee against an account.
    public void chargeFee(Long accountId, double feeAmount) {
        Account account = accountRepository.findOne(accountId);
        double newBalance = account.getBalance() - feeAmount;
        account.setBalance(newBalance);
        accountRepository.save(account);
    }

    // Free-text search for accounts by customer name fragment.
    // Uses a raw JDBC query against the same database.
    public List<Map<String, Object>> searchByCustomerName(String nameFragment) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        String sql = "SELECT a.ACCOUNT_NUMBER, a.ACCOUNT_TYPE, a.BALANCE, c.FULL_NAME "
                + "FROM ACCOUNT a "
                + "JOIN CUSTOMER c ON c.CUSTOMER_ID = a.CUSTOMER_ID "
                + "WHERE c.FULL_NAME LIKE '%" + nameFragment + "%'";

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        rows.addAll(jdbc.queryForList(sql));
        return rows;
    }
}
