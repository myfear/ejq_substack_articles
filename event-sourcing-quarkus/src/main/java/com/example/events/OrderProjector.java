package com.example.events;

import java.util.List;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderProjector {

    @Inject
    EventStore eventStore;

    @Transactional
    void on(@Observes OrderEvent event) {
        UUID orderId = event.orderId();

        // Recompute current state from all events for this order
        List<OrderEvent> events = eventStore.loadEvents(orderId);
        OrderState state = EventProjection.replayEvents(events);

        OrderReadModel readModel = OrderReadModel.findByOrderId(orderId);
        if (readModel == null) {
            readModel = new OrderReadModel();
            readModel.orderId = orderId;
        }

        readModel.customerEmail = state.customerEmail();
        readModel.status = state.status().name();
        readModel.total = state.total();
        readModel.lastUpdated = event.timestamp();
        readModel.persist();
    }
}