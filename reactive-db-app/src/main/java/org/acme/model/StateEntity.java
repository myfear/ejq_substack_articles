package org.acme.model;

import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "wizard_state")
public class StateEntity extends PanacheEntityBase {
@Id
    @GeneratedValue
    @UuidGenerator
    public String id;

    @JdbcTypeCode(SqlTypes.JSON)
    public State state; 

    @Column(name = "current_step")
    public int currentStep;

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "expires_at")
    public Instant expiresAt; 
    
    public StateEntity() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.expiresAt = Instant.now().plusSeconds(3600); // Default expiration time of 1 hour
    }

}
