package com.acme.claims.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "processed_event")
public class ProcessedEvent extends PanacheEntityBase {

    @Id
    public String eventId;

    public static boolean alreadyProcessed(String eventId) {
        return findById(eventId) != null;
    }

    public static void markProcessed(String eventId) {
        ProcessedEvent e = new ProcessedEvent();
        e.eventId = eventId;
        e.persist();
    }
}
