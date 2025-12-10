package com.example.events;

import static jakarta.transaction.Transactional.TxType.SUPPORTS;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EventStore {

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
        stored.eventType = eventType(event);
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

    private String eventType(OrderEvent event) {
        return switch (event) {
            case OrderEvent.OrderPlaced ignored -> "OrderPlaced";
            case OrderEvent.ItemAdded ignored -> "ItemAdded";
            case OrderEvent.ItemRemoved ignored -> "ItemRemoved";
            case OrderEvent.OrderCancelled ignored -> "OrderCancelled";
            case OrderEvent.OrderShipped ignored -> "OrderShipped";
        };
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
            return switch (stored.eventType) {
                case "OrderPlaced" ->
                    objectMapper.readValue(stored.eventData, OrderEvent.OrderPlaced.class);
                case "ItemAdded" ->
                    objectMapper.readValue(stored.eventData, OrderEvent.ItemAdded.class);
                case "ItemRemoved" ->
                    objectMapper.readValue(stored.eventData, OrderEvent.ItemRemoved.class);
                case "OrderCancelled" ->
                    objectMapper.readValue(stored.eventData, OrderEvent.OrderCancelled.class);
                case "OrderShipped" ->
                    objectMapper.readValue(stored.eventData, OrderEvent.OrderShipped.class);
                default ->
                    throw new IllegalArgumentException("Unknown event type " + stored.eventType);
            };
        } catch (IOException e) {
            throw new IllegalStateException("Could not deserialize event " + stored.id, e);
        }
    }
}