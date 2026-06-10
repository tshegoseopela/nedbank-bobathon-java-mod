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

// A single posting against an account. Double-entry: every transfer produces
// one DEBIT entry and one CREDIT entry.
@Entity
@Table(name = "LEDGER_ENTRY")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ENTRY_ID")
    private Long id;

    @Column(name = "ACCOUNT_ID")
    private Long accountId;

    // "DEBIT" or "CREDIT"
    @Column(name = "ENTRY_TYPE")
    private String entryType;

    @Column(name = "AMOUNT")
    private double amount;

    @Column(name = "DESCRIPTION")
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "POSTED_AT")
    private Date postedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getEntryType() { return entryType; }
    public void setEntryType(String entryType) { this.entryType = entryType; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getPostedAt() { return postedAt; }
    public void setPostedAt(Date postedAt) { this.postedAt = postedAt; }
}
