package com.example.events;

import static jakarta.transaction.Transactional.TxType.SUPPORTS;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.events.Commands.AddItemCommand;
import com.example.events.Commands.CancelOrderCommand;
import com.example.events.Commands.PlaceOrderCommand;
import com.example.events.Commands.ShipOrderCommand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrderCommandHandler {

    private static final String AGGREGATE_TYPE = "Order";

    @Inject
    EventStore eventStore;

    @Transactional
    public CommandResult placeOrder(PlaceOrderCommand cmd) {
        if (cmd.customerEmail() == null || cmd.customerEmail().isBlank()) {
            return new CommandResult.ValidationError("Customer email must not be empty");
        }

        UUID orderId = UUID.randomUUID();
        var event = new OrderEvent.OrderPlaced(
                orderId,
                cmd.customerEmail(),
                Instant.now());
        eventStore.append(orderId, AGGREGATE_TYPE, event);
        return new CommandResult.Success(orderId);
    }

    @Transactional
    public CommandResult addItem(AddItemCommand cmd) {
        List<OrderEvent> events = eventStore.loadEvents(cmd.orderId());
        if (events.isEmpty()) {
            return new CommandResult.NotFound("Order not found: " + cmd.orderId());
        }

        OrderState current = EventProjection.replayEvents(events);
        if (current.status() != OrderStatus.DRAFT) {
            return new CommandResult.InvalidState(
                    "Cannot add items to order in status " + current.status());
        }

        if (cmd.quantity() <= 0) {
            return new CommandResult.ValidationError("Quantity must be positive");
        }

        var event = new OrderEvent.ItemAdded(
                cmd.orderId(),
                cmd.productName(),
                cmd.quantity(),
                cmd.price(),
                Instant.now());
        eventStore.append(cmd.orderId(), AGGREGATE_TYPE, event);
        return new CommandResult.Success(cmd.orderId());
    }

    @Transactional
    public CommandResult shipOrder(ShipOrderCommand cmd) {
        List<OrderEvent> events = eventStore.loadEvents(cmd.orderId());
        if (events.isEmpty()) {
            return new CommandResult.NotFound("Order not found: " + cmd.orderId());
        }

        OrderState current = EventProjection.replayEvents(events);
        if (current.status() != OrderStatus.DRAFT) {
            return new CommandResult.InvalidState(
                    "Only DRAFT orders can be shipped. Current status: " + current.status());
        }

        var event = new OrderEvent.OrderShipped(
                cmd.orderId(),
                cmd.trackingNumber(),
                Instant.now());
        eventStore.append(cmd.orderId(), AGGREGATE_TYPE, event);
        return new CommandResult.Success(cmd.orderId());
    }

    @Transactional
    public CommandResult cancelOrder(CancelOrderCommand cmd) {
        List<OrderEvent> events = eventStore.loadEvents(cmd.orderId());
        if (events.isEmpty()) {
            return new CommandResult.NotFound("Order not found: " + cmd.orderId());
        }

        OrderState current = EventProjection.replayEvents(events);
        if (current.status() == OrderStatus.SHIPPED) {
            return new CommandResult.InvalidState("Cannot cancel shipped order");
        }

        var event = new OrderEvent.OrderCancelled(
                cmd.orderId(),
                cmd.reason(),
                Instant.now());
        eventStore.append(cmd.orderId(), AGGREGATE_TYPE, event);
        return new CommandResult.Success(cmd.orderId());
    }

    @Transactional(SUPPORTS)
    public OrderState loadCurrentState(UUID orderId) {
        List<OrderEvent> events = eventStore.loadEvents(orderId);
        if (events.isEmpty()) {
            return null;
        }
        return EventProjection.replayEvents(events);
    }
}