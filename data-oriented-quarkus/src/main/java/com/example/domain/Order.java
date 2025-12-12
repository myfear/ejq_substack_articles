package com.example.domain;

import java.math.BigDecimal;
import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order extends PanacheEntity {
    public String customerEmail;
    public String productName;
    public Integer quantity;
    public BigDecimal totalAmount;
    public String status;
    public Instant createdAt;
}