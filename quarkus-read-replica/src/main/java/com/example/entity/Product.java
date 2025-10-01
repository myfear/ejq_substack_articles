package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product extends PanacheEntity {
    @Column(nullable = false)
    public String name;
    @Column(nullable = false, precision = 10, scale = 2)
    public BigDecimal price;
    @Column(columnDefinition = "TEXT")
    public String description;
    @Column(length = 100)
    public String category;
    @Column(name = "stock_quantity")
    public Integer stockQuantity = 0;
    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at")
    public LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}