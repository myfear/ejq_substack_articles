package com.example.events;

import java.time.Instant;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_store", indexes = {
        @Index(name = "idx_event_aggregate", columnList = "aggregateId, version")
})
public class StoredEvent extends PanacheEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    public UUID aggregateId;

    @Column(nullable = false, length = 64)
    public String aggregateType;

    @Column(nullable = false)
    public long version;

    @Column(nullable = false, length = 64)
    public String eventType;

    @Column(nullable = false, columnDefinition = "text")
    public String eventData;

    @Column(nullable = false)
    public Instant timestamp;
}
