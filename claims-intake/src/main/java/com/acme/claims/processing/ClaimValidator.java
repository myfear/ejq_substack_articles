package com.acme.claims.processing;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.acme.claims.events.ClaimSubmitted;
import com.acme.claims.events.ClaimValidated;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClaimValidator {

    @Incoming("claims-submitted-in")
    @Outgoing("claims-validated-out")
    public ClaimValidated validate(ClaimSubmitted claim) {

        if (claim.amount() <= 0) {
            throw new IllegalArgumentException(
                    "Claim amount must be positive");
        }

        return new ClaimValidated(
                claim.eventId(),
                claim.claimId(),
                claim.customerId(),
                claim.amount());
    }
}