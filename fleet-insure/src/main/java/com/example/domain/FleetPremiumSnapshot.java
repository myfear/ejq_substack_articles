package com.example.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonBackReference;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "fleet_premium_snapshot", indexes = {
        @Index(name = "idx_snapshot_policy_asof", columnList = "policy_id, asOf")
})
public class FleetPremiumSnapshot extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonBackReference
    public FleetPolicy policy;

    @Column(nullable = false)
    public java.time.LocalDate asOf; // <-- NEW

    @Column(nullable = false)
    public LocalDateTime calculationDate;

    @Column(nullable = false, precision = 18, scale = 2)
    public BigDecimal totalPremium;

    @Column(nullable = false, length = 128)
    public String calculationTrigger;

    @ManyToOne(fetch = FetchType.LAZY)
    public FleetPremiumSnapshot previousSnapshot;

    // Lightweight custom dirty-check guard. If unchanged, skip creating a new
    // snapshot.
    @Column(nullable = false, length = 64)
    public String riskHash;

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<VehicleShare> vehicleShares;

    @OneToMany(mappedBy = "snapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<SnapshotReinsuranceAllocation> reinsuranceAllocations;

    // Month -> exposure units captured at calculation time
    @ElementCollection
    @CollectionTable(name = "snapshot_exposure", joinColumns = @JoinColumn(name = "snapshot_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "units", nullable = false, precision = 18, scale = 4)
    public Map<java.time.Month, java.math.BigDecimal> exposureUnitsByMonth;
}