package com.example.api;

import java.math.BigDecimal;

// Sealed result type
public sealed interface OrderResult
                permits OrderResult.Success,
                OrderResult.OutOfStock,
                OrderResult.ProductNotFound,
                OrderResult.InvalidRequest {

        record Success(
                        Long orderId,
                        String customerEmail,
                        String productName,
                        Integer quantity,
                        BigDecimal totalAmount) implements OrderResult {
        }

        record OutOfStock(
                        String productName,
                        Integer available,
                        Integer requested) implements OrderResult {
        }

        record ProductNotFound(String productName) implements OrderResult {
        }

        record InvalidRequest(String message) implements OrderResult {
        }
}
