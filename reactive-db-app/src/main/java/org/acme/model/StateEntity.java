package org.acme.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "wizard_state")
public class StateEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    public String id;

    @JdbcTypeCode(SqlTypes.JSON)
    public State state;

    public int currentStep;
    public Instant createdAt;
    public Instant updatedAt;
    public Instant expiresAt;

    public StateEntity() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.expiresAt = Instant.now().plusSeconds(3600); // Default expiration time of 1 hour
    }

}
