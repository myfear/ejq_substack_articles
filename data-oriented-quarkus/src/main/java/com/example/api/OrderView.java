package com.example.api;

import java.math.BigDecimal;

// Read model for queries
public record OrderView(
                Long id,
                String customerEmail,
                String productName,
                Integer quantity,
                BigDecimal totalAmount,
                String status,
                String createdAt) {
}
