package com.example.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;

@Entity
public class TransactionLog extends PanacheEntity {
    public Long customerId;
    public Long fromAccountId;
    public Long toAccountId;
    public BigDecimal amount;
    public String transactionType; // TRANSFER, DEPOSIT, WITHDRAWAL, FREEZE, UNFREEZE
    public String status; // SUCCESS, FAILED, PENDING
    public String description;
    public LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        timestamp = LocalDateTime.now();
    }
}