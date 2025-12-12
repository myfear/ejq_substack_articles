package com.example.api;

// Input from client
public record OrderRequest(
        String customerEmail,
        String productName,
        Integer quantity) {
    public OrderRequest {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (customerEmail == null || customerEmail.isBlank()) {
            throw new IllegalArgumentException("Customer email required");
        }
    }
}

