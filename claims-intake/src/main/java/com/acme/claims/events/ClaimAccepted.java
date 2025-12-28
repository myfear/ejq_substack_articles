package com.acme.claims.events;

public record ClaimAccepted(
        String eventId,
        String claimId,
        String status) {
}