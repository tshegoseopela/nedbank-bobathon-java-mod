package com.aurorabank.core.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "ACCOUNT")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ACCOUNT_ID")
    private Long id;

    @Column(name = "ACCOUNT_NUMBER")
    private String accountNumber;

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    // "CHECKING" or "SAVINGS"
    @Column(name = "ACCOUNT_TYPE")
    private String accountType;

    // ISO currency code, e.g. "EUR"
    @Column(name = "CURRENCY")
    private String currency;

    // NOTE: balance is stored as a primitive double (legacy design).
    @Column(name = "BALANCE")
    private double balance;

    // "ACTIVE", "FROZEN", "CLOSED"
    @Column(name = "STATUS")
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "OPENED_AT")
    private Date openedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getOpenedAt() { return openedAt; }
    public void setOpenedAt(Date openedAt) { this.openedAt = openedAt; }
}
