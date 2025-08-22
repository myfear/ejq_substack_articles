package com.example.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "fleet_policy")
public class FleetPolicy extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    @Version
    public int version;

    @Column(nullable = false, unique = true)
    public String policyNumber;

    @Column(nullable = false)
    public String customer;

    @Column(nullable = false, precision = 18, scale = 2)
    public BigDecimal coverageLimit;

    @Column(nullable = false)
    public LocalDate effectiveFrom;

    @Column(nullable = false)
    public LocalDate effectiveTo;

    // Optional: rate lock
    public LocalDate rateLockUntil;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    public List<PolicyVehicle> vehicles; // temporal membership

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("asOf ASC, id ASC") 
    @JsonManagedReference
    public List<FleetPremiumSnapshot> snapshots;

    @OneToMany(mappedBy = "policy", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    public List<AuditEntry> auditEntries;
}