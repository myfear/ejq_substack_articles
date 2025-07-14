package com.support;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class SupportTicket extends PanacheEntity {
    public String customerName;
    public String customerEmail;
    public String subject;
    public String description;
    public String imageUrl;

    @Enumerated(EnumType.STRING)
    public TicketStatus status = TicketStatus.CREATED;

    public LocalDateTime createdAt = LocalDateTime.now();

    public enum TicketStatus {
        CREATED, AI_CLASSIFICATION, SOLUTION_LOOKUP, AGENT_ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
    }
}
