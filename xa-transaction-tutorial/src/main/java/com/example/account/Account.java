package com.example.account;

import java.math.BigDecimal;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Account extends PanacheEntity {
    public Long customerId;
    public BigDecimal balance;
}