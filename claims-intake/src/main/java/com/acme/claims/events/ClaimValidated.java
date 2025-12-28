package com.acme.claims.events;

public record ClaimValidated(
        String eventId,
        String claimId,
        String customerId,
        double amount) {
}