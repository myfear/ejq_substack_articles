package com.acme.claims.processing;

import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.acme.claims.events.ClaimValidated;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClaimEnricher {

    private static final int MAX_ATTEMPTS = 3;

    @Incoming("claims-validated-in")
    @Outgoing("claims-enriched-out")
    public ClaimValidated enrich(ClaimValidated claim) {

        int attempt = 0;

        while (true) {
            try {
                attempt++;
                callExternalService(claim);
                return claim;
            } catch (RuntimeException ex) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw ex; // routed to DLQ
                }
                backoff(attempt);
            }
        }
    }

    private void callExternalService(ClaimValidated claim) {
        if (ThreadLocalRandom.current().nextInt(4) == 0) {
            throw new RuntimeException("External service timeout");
        }
    }

    private void backoff(int attempt) {
        try {
            Thread.sleep(500L * attempt);
        } catch (InterruptedException ignored) {
        }
    }
}