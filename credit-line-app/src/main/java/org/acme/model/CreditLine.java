package org.acme.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDateTime;

@Entity
public class CreditLine extends PanacheEntity {

    public String customerName;
    public String customerEmail;
    public double requestedAmount;

    @Enumerated(EnumType.STRING)
    public CreditLineState state;

    public LocalDateTime creationTimestamp;
    public LocalDateTime lastUpdatedTimestamp;
    public LocalDateTime approvalTimestamp;
    public LocalDateTime emailSentTimestamp;

    // Default constructor for Panache
    public CreditLine() {
    }

    public CreditLine(String customerName, String customerEmail, double requestedAmount) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.requestedAmount = requestedAmount;
        this.state = CreditLineState.INITIATED;
        this.creationTimestamp = LocalDateTime.now();
        this.lastUpdatedTimestamp = LocalDateTime.now();
    }
}
