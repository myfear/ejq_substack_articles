package com.example.events;

import java.math.BigDecimal;
import java.util.UUID;

public final class Commands {

    private Commands() {
    }

    public record PlaceOrderCommand(
            String customerEmail) {
    }

    public record AddItemCommand(
            UUID orderId,
            String productName,
            int quantity,
            BigDecimal price) {
    }

    public record ShipOrderCommand(
            UUID orderId,
            String trackingNumber) {
    }

    public record CancelOrderCommand(
            UUID orderId,
            String reason) {
    }
}