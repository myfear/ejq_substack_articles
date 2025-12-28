package com.acme.claims.processing;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.acme.claims.events.ClaimAccepted;
import com.acme.claims.events.ClaimValidated;
import com.acme.claims.persistence.ProcessedEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ClaimPersister {

    @Incoming("claims-enriched-in")
    @Outgoing("claims-accepted-out")
    @Transactional
    public ClaimAccepted persist(ClaimValidated claim) {

        if (ProcessedEvent.alreadyProcessed(claim.eventId())) {
            return null; // duplicate, safely ignored
        }

        ProcessedEvent.markProcessed(claim.eventId());

        return new ClaimAccepted(
                claim.eventId(),
                claim.claimId(),
                "ACCEPTED");
    }
}
