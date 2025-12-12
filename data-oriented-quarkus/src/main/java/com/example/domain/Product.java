package com.example.domain;

import java.math.BigDecimal;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product extends PanacheEntity {
    public String name;
    public BigDecimal price;
    public Integer stockQuantity;
}