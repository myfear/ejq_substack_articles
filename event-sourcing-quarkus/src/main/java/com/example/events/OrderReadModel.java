package com.example.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_read_model")
public class OrderReadModel extends PanacheEntity {

    @Column(nullable = false, columnDefinition = "uuid", unique = true)
    public UUID orderId;

    @Column(nullable = false)
    public String customerEmail;

    @Column(nullable = false)
    public String status;

    @Column(nullable = false)
    public BigDecimal total;

    @Column(nullable = false)
    public Instant lastUpdated;

    public static OrderReadModel findByOrderId(UUID orderId) {
        return find("orderId", orderId).firstResult();
    }
}
