package org.acme.wizard.model;

import java.time.Duration;
import java.time.Instant;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "wizard_state")
public class WizardStateEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    String id;

    @JdbcTypeCode(SqlTypes.JSON)
    public WizardState wizardState; // The serialized WizardState

    
    public int currentStep;
    public Instant createdAt;
    public Instant updatedAt;
    public Instant expiresAt; // For scheduled cleanup

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public WizardState getWizardState() {
        return wizardState;
    }

    public void setWizardState(WizardState wizardState) {
        this.wizardState = wizardState;

    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        expiresAt = Instant.now().plus(Duration.ofHours(2)); // State expires in 2 hours

    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        expiresAt = Instant.now().plus(Duration.ofHours(2)); // Re-extend expiration on update
    }

}