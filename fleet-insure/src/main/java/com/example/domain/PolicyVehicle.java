package com.example.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "policy_vehicle", uniqueConstraints = @UniqueConstraint(columnNames = { "policy_id", "vehicle_id",
        "effectiveFrom" }))
public class PolicyVehicle extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonBackReference
    public FleetPolicy policy;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public Vehicle vehicle;

    @Column(nullable = false)
    public LocalDate effectiveFrom;

    // null = still active
    public LocalDate effectiveTo;
}