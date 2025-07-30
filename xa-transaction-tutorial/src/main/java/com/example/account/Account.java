package com.example.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Account extends PanacheEntity {
    public Long customerId;
    public BigDecimal balance;
    public String accountType = "CHECKING"; // CHECKING, SAVINGS, CREDIT
    public String status = "ACTIVE"; // ACTIVE, FROZEN, CLOSED
    public BigDecimal dailyLimit = new BigDecimal("5000.00");
    public BigDecimal availableLimit = new BigDecimal("5000.00");
    public LocalDateTime lastTransactionDate;
    public Integer transactionCount = 0;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastTransactionDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}