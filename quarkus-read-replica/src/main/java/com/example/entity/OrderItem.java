package com.example.entity;

import java.math.BigDecimal;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderItem extends PanacheEntity {
    @Column(name = "order_id", nullable = false)
    public Long orderId;
    @Column(name = "product_id", nullable = false)
    public Long productId;
    @Column(nullable = false)
    public Integer quantity;
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    public BigDecimal unitPrice;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    public Product product;
}
