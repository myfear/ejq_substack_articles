package com.example.events;

import com.fasterxml.jackson.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

// Sealed interface: the compiler knows all event types
public sealed interface OrderEvent
        permits OrderEvent.OrderPlaced,
        OrderEvent.ItemAdded,
        OrderEvent.ItemRemoved,
        OrderEvent.OrderCancelled,
        OrderEvent.OrderShipped {

    UUID orderId();

    Instant timestamp();

    OrderState applyTo(OrderState state);

    String getEventType();

    Class<? extends OrderEvent> getEventClass();

    record OrderPlaced(
            UUID orderId,
            String customerEmail,
            Instant timestamp) implements OrderEvent {
        @Override
        public OrderState applyTo(OrderState state) {
            return OrderState.initial(orderId(), customerEmail());
        }

        @Override
        public String getEventType() {
            return "OrderPlaced";
        }

        @Override
        public Class<? extends OrderEvent> getEventClass() {
            return OrderPlaced.class;
        }
    }

    record ItemAdded(
            UUID orderId,
            @JsonProperty("productName")
            String productName,
            int quantity,
            BigDecimal price,
            Instant timestamp) implements OrderEvent {
        @Override
        public OrderState applyTo(OrderState state) {
            var items = new ArrayList<>(state.items());
            items.add(new OrderLine(
              productName(),
              quantity(),
              price()));
            var newTotal = state.total().add(
              price().multiply(BigDecimal.valueOf(quantity())));
            return new OrderState(
              state.orderId(),
              state.customerEmail(),
              List.copyOf(items),
              state.status(),
              newTotal);
        }

        @Override
        public String getEventType() {
            return "ItemAdded";
        }

        @Override
        public Class<? extends OrderEvent> getEventClass() {
            return ItemAdded.class;
        }
    }

    record ItemRemoved(
            UUID orderId,
            @JsonProperty("productName")
            String productName,
            int quantity,
            BigDecimal price,
            Instant timestamp) implements OrderEvent {
        @Override
        public OrderState applyTo(OrderState state) {
            var items = new ArrayList<>(state.items());
            // very naive removal: remove first matching product
            items.removeIf(line -> line.productName().equals(productName())
              && line.price().compareTo(price()) == 0);
            var newTotal = state.total().subtract(
              price().multiply(BigDecimal.valueOf(quantity())));
            return new OrderState(
              state.orderId(),
              state.customerEmail(),
              List.copyOf(items),
              state.status(),
              newTotal);
        }

        @Override
        public String getEventType() {
            return "ItemRemoved";
        }

        @Override
        public Class<? extends OrderEvent> getEventClass() {
            return ItemRemoved.class;
        }
    }

    record OrderCancelled(
            UUID orderId,
            String reason,
            Instant timestamp) implements OrderEvent {
        @Override
        public OrderState applyTo(OrderState state) {
            return new OrderState(
              state.orderId(),
              state.customerEmail(),
              state.items(),
              OrderStatus.CANCELLED,
              state.total());
        }

        @Override
        public String getEventType() {
            return "OrderCanceled";
        }

        @Override
        public Class<? extends OrderEvent> getEventClass() {
            return OrderCancelled.class;
        }
    }

    record OrderShipped(
            UUID orderId,
            @JsonProperty("trackingNumber")
            String trackingNumber,
            Instant timestamp) implements OrderEvent {
        @Override
        public OrderState applyTo(OrderState state) {
            return new OrderState(
              state.orderId(),
              state.customerEmail(),
              state.items(),
              OrderStatus.SHIPPED,
              state.total());
        }

        @Override
        public String getEventType() {
            return "OrderShipped";
        }

        @Override
        public Class<? extends OrderEvent> getEventClass() {
            return OrderShipped.class;
        }
    }
}
