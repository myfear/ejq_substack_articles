package com.example.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderState(
        UUID orderId,
        String customerEmail,
        List<OrderLine> items,
        OrderStatus status,
        BigDecimal total) {

    public static OrderState initial(UUID id, String email) {
        return new OrderState(
                id,
                email,
                List.of(),
                OrderStatus.DRAFT,
                BigDecimal.ZERO);
    }

    public static OrderState empty() {
        return new OrderState(
                null,
                null,
                List.of(),
                OrderStatus.DRAFT,
                BigDecimal.ZERO);
    }
}