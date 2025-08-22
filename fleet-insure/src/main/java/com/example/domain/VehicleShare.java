package com.example.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_share", indexes = @Index(name = "idx_vehicle_share_snapshot", columnList = "snapshot_id"))
public class VehicleShare extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonBackReference
    public FleetPremiumSnapshot snapshot;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public Vehicle vehicle;

    @Column(nullable = false)
    public int riskScore;

    @Column(nullable = false, precision = 9, scale = 6)
    public BigDecimal fleetPercentage; // 0..1

    @Column(nullable = false, precision = 18, scale = 2)
    public BigDecimal premiumContribution;

    @Column(nullable = false, precision = 18, scale = 4)
    public BigDecimal exposureUnits;

    public LocalDateTime effectiveFromDate;
    public LocalDateTime effectiveToDate;
}