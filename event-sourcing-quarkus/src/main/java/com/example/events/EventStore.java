package com.example.events;

import static jakarta.transaction.Transactional.TxType.SUPPORTS;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EventStore {
    private static final Map<String, Class<? extends OrderEvent>> EVENT_TYPES =
      Map.ofEntries(
        Map.entry("OrderPlaced", OrderEvent.OrderPlaced.class),
        Map.entry("ItemAdded", OrderEvent.ItemAdded.class),
        Map.entry("ItemRemoved", OrderEvent.ItemRemoved.class),
        Map.entry("OrderCancelled", OrderEvent.OrderCancelled.class),
        Map.entry("OrderShipped", OrderEvent.OrderShipped.class)
      );

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Event<OrderEvent> orderEventBus;

    @Transactional
    public void append(UUID aggregateId, String aggregateType, OrderEvent event) {
        StoredEvent stored = new StoredEvent();
        stored.aggregateId = aggregateId;
        stored.aggregateType = aggregateType;
        stored.version = nextVersion(aggregateId);
        stored.eventType = event.getEventType();
        stored.eventData = serialize(event);
        stored.timestamp = event.timestamp();
        stored.persist();

        // Publish CDI event for projections and other listeners
        orderEventBus.fire(event);
    }

    @Transactional(SUPPORTS)
    public List<OrderEvent> loadEvents(UUID aggregateId) {
        List<StoredEvent> rows = StoredEvent.list(
                "aggregateId = ?1 ORDER BY version",
                aggregateId);
        return rows.stream()
                .map(this::deserialize)
                .toList();
    }

    private long nextVersion(UUID aggregateId) {
        long count = StoredEvent.count("aggregateId", aggregateId);
        return count + 1;
    }

    private String serialize(OrderEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize event " + event, e);
        }
    }

    private OrderEvent deserialize(StoredEvent stored) {
        try {
            Class<? extends OrderEvent> eventClass = EVENT_TYPES.get(stored.eventType);
            if (eventClass == null) {
                throw new IllegalArgumentException("Unknown event type: " + stored.eventType);
            }
            return objectMapper.readValue(stored.eventData, eventClass);
        } catch (IOException e) {
            throw new IllegalStateException("Could not deserialize event " + stored.id, e);
        }
    }
}