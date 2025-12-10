package com.example.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class EventProjectionTest {

    @Test
    void testOrderLifecycle() {
        UUID orderId = UUID.randomUUID();
        Instant now = Instant.now();
        BigDecimal price = new BigDecimal("999.99");

        List<OrderEvent> events = List.of(
                new OrderEvent.OrderPlaced(orderId, "test@example.com", now),
                new OrderEvent.ItemAdded(orderId, "Laptop", 1, price, now),
                new OrderEvent.OrderShipped(orderId, "TRACK-123", now));

        OrderState finalState = EventProjection.replayEvents(events);

        assertEquals(orderId, finalState.orderId());
        assertEquals("test@example.com", finalState.customerEmail());
        assertEquals(OrderStatus.SHIPPED, finalState.status());
        assertEquals(price, finalState.total());
    }
}
