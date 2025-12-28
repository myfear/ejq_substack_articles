package com.acme.claims.events;

public record ClaimSubmitted(
        String eventId,
        String claimId,
        String customerId,
        double amount) {
}