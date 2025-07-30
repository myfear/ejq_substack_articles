package com.example.customer;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

@Entity
public class Customer extends PanacheEntity {
    public String name;
    public String email;
    public String phone;
    public String status = "STANDARD"; // STANDARD, PREMIUM, SUSPENDED
    public Integer loyaltyPoints = 0;
    public LocalDateTime lastLoginDate;
    public Integer totalTransactions = 0;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
