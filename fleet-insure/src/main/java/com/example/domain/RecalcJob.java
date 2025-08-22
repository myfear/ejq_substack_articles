package com.example.domain;

import java.time.OffsetDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recalc_job")
public class RecalcJob extends PanacheEntityBase {

    public enum Status {
        QUEUED, RUNNING, DONE, FAILED
    }

    @Id
    @GeneratedValue
    public Long id;

    public Long policyId;
    public String trigger;
    public String message;

    @Enumerated(EnumType.STRING)
    public Status status;

    public OffsetDateTime createdAt = OffsetDateTime.now();
    public OffsetDateTime finishedAt;
}