package com.acme.model;

public record Customer(
        String id,
        String name,
        String email,
        String status,
        String recentNote) {
}