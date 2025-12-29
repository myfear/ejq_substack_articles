package com.acme.claims.events;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ClaimValidated(
                @JsonProperty("eventId") String eventId,
                @JsonProperty("claimId") String claimId,
                @JsonProperty("customerId") String customerId,
                @JsonProperty("amount") double amount) {
}