package org.acme.wizard.model;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.Arc;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "wizard_state")
public class WizardStateEntity  {

    @Id
    @GeneratedValue
    @UuidGenerator
    String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStateDataJson() {
        return stateDataJson;
    }

    public void setStateDataJson(String stateDataJson) {
        this.stateDataJson = stateDataJson;
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

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    // Use columnDefinition = "jsonb" for PostgreSQL's binary JSON type for
    // efficiency
    @Column(name = "state_data", columnDefinition = "jsonb")
    public String stateDataJson; // The serialized WizardState

    @Column(name = "current_step")
    public int currentStep;

    @Column(name = "created_at")
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "expires_at")
    public Instant expiresAt; // For scheduled cleanup

    @Column(name = "user_id") // Optional: link to a logged-in user
    public UUID userId; // Example type, adjust based on your User entity's ID

    // Transient field to hold the deserialized WizardState object in memory
    @Transient
    private WizardState wizardState;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString(); // Generate UUID if not already set
        }
        createdAt = Instant.now();
        updatedAt = Instant.now();
        expiresAt = Instant.now().plus(Duration.ofHours(2)); // State expires in 2 hours
        serializeWizardState();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        expiresAt = Instant.now().plus(Duration.ofHours(2)); // Re-extend expiration on update
        serializeWizardState();
    }

    // Helper to serialize WizardState to JSON string before persisting
    private void serializeWizardState() {
        if (wizardState != null) {
            try {
                // Get ObjectMapper from CDI context
                ObjectMapper objectMapper = Arc.container().instance(ObjectMapper.class).get();
                stateDataJson = objectMapper.writeValueAsString(wizardState);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize wizardState to JSON", e);
            }
        }
    }

    // Helper to deserialize WizardState from JSON string after loading
    public WizardState getWizardState() {
        if (wizardState == null && stateDataJson != null) {
            try {
                // Get ObjectMapper from CDI context
                ObjectMapper objectMapper = Arc.container().instance(ObjectMapper.class).get();
                wizardState = objectMapper.readValue(stateDataJson, WizardState.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize wizardState from JSON", e);
            }
        }
        return wizardState;
    }

    // Setter for the transient wizardState object
    public void setWizardState(WizardState wizardState) {
        this.wizardState = wizardState;
        this.currentStep = wizardState.getCurrentStep(); // Keep currentStep in sync
        serializeWizardState(); // Immediately serialize when the state object is set
    }
}