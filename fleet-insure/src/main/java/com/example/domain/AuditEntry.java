package com.example.domain;

import java.time.OffsetDateTime;

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
@Table(name = "audit_entry", indexes = @Index(name = "idx_audit_policy_date", columnList = "policy_id,createdAt"))
public class AuditEntry extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonBackReference
    public FleetPolicy policy;

    @Column(nullable = false)
    public String reason;

    @Column(nullable = false)
    public String trigger; // endpoint/event name

    @Column(nullable = false)
    public OffsetDateTime createdAt = OffsetDateTime.now();
}