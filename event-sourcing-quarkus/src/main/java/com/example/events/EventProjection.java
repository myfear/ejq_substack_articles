package com.example.events;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class EventProjection {

    private EventProjection() {
        // utility class
    }

    public static OrderState apply(OrderState state, OrderEvent event) {
        return event.applyTo(state);
    }

    public static OrderState replayEvents(List<OrderEvent> events) {
        return events.stream()
          .reduce(
            OrderState.empty(),
            EventProjection::apply,
            (left, right) -> right);
    }
}