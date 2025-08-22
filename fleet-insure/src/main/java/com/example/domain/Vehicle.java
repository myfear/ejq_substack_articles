package com.example.domain;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "vehicle")
public class Vehicle extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    @Version
    public int version;

    @Column(nullable = false, unique = true, length = 17)
    public String vin;

    public String makeModel;
    public LocalDate purchaseDate;

    // Current risk score (0..100). Changes over time through business events.
    @Column(nullable = false)
    public int currentRiskScore;

    // Simple usage label for demo: URBAN, HIGHWAY, MIXED
    public String usageProfile;

    public OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = OffsetDateTime.now();
    }
}