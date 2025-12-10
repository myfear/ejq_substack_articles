package com.example.events;

import java.math.BigDecimal;

public record OrderLine(
        String productName,
        int quantity,
        BigDecimal price) {
    public BigDecimal lineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}