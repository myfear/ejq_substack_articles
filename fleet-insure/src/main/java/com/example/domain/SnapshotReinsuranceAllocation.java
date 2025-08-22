package com.example.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "snapshot_re_allocation")
public class SnapshotReinsuranceAllocation extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonBackReference
    public FleetPremiumSnapshot snapshot;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    public ReinsuranceLayer layer;

    @Column(nullable = false, precision = 18, scale = 2)
    public BigDecimal allocatedAmount;
}