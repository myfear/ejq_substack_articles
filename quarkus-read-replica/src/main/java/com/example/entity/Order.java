package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order extends PanacheEntity {
    @Column(name = "customer_name", nullable = false)
    public String customerName;
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    public BigDecimal totalAmount;
    @Column(length = 50)
    public String status = "PENDING";
    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at")
    public LocalDateTime updatedAt = LocalDateTime.now();
    @OneToMany(mappedBy = "orderId", fetch = FetchType.LAZY)
    public List<OrderItem> items;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}