package com.example.events;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class EventProjection {

    private EventProjection() {
        // utility class
    }

    public static OrderState apply(OrderState state, OrderEvent event) {
        return switch (event) {
            case OrderEvent.OrderPlaced e -> OrderState.initial(
                    e.orderId(),
                    e.customerEmail());

            case OrderEvent.ItemAdded e -> {
                var items = new ArrayList<>(state.items());
                items.add(new OrderLine(
                        e.productName(),
                        e.quantity(),
                        e.price()));
                var newTotal = state.total().add(
                        e.price().multiply(BigDecimal.valueOf(e.quantity())));
                yield new OrderState(
                        state.orderId(),
                        state.customerEmail(),
                        List.copyOf(items),
                        state.status(),
                        newTotal);
            }

            case OrderEvent.ItemRemoved e -> {
                var items = new ArrayList<>(state.items());
                // very naive removal: remove first matching product
                items.removeIf(line -> line.productName().equals(e.productName())
                        && line.price().compareTo(e.price()) == 0);
                var newTotal = state.total().subtract(
                        e.price().multiply(BigDecimal.valueOf(e.quantity())));
                yield new OrderState(
                        state.orderId(),
                        state.customerEmail(),
                        List.copyOf(items),
                        state.status(),
                        newTotal);
            }

            case OrderEvent.OrderCancelled e -> new OrderState(
                    state.orderId(),
                    state.customerEmail(),
                    state.items(),
                    OrderStatus.CANCELLED,
                    state.total());

            case OrderEvent.OrderShipped e -> new OrderState(
                    state.orderId(),
                    state.customerEmail(),
                    state.items(),
                    OrderStatus.SHIPPED,
                    state.total());
        };
    }

    public static OrderState replayEvents(List<OrderEvent> events) {
        return events.stream()
                .reduce(
                        OrderState.empty(),
                        EventProjection::apply,
                        (left, right) -> right);
    }
}