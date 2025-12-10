package com.example.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Sealed interface: the compiler knows all event types
public sealed interface OrderEvent
        permits OrderEvent.OrderPlaced,
        OrderEvent.ItemAdded,
        OrderEvent.ItemRemoved,
        OrderEvent.OrderCancelled,
        OrderEvent.OrderShipped {

    UUID orderId();

    Instant timestamp();

    record OrderPlaced(
            UUID orderId,
            String customerEmail,
            Instant timestamp) implements OrderEvent {
    }

    record ItemAdded(
            UUID orderId,
            String productName,
            int quantity,
            BigDecimal price,
            Instant timestamp) implements OrderEvent {
    }

    record ItemRemoved(
            UUID orderId,
            String productName,
            int quantity,
            BigDecimal price,
            Instant timestamp) implements OrderEvent {
    }

    record OrderCancelled(
            UUID orderId,
            String reason,
            Instant timestamp) implements OrderEvent {
    }

    record OrderShipped(
            UUID orderId,
            String trackingNumber,
            Instant timestamp) implements OrderEvent {
    }
}