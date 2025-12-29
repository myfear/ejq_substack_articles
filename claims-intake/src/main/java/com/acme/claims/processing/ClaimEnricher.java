package com.acme.claims.processing;

import java.util.concurrent.ThreadLocalRandom;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.acme.claims.events.ClaimValidated;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClaimEnricher {

    private static final int MAX_ATTEMPTS = 3;

    @Incoming("validated")
    @Outgoing("claims-enriched")
    public ClaimValidated enrich(ClaimValidated claim) {

        Log.infof("🔍 [ENRICHING] eventId=%s, claimId=%s - Calling external service...", 
                claim.eventId(), claim.claimId());

        int attempt = 0;

        while (true) {
            try {
                attempt++;
                if (attempt > 1) {
                    Log.infof("🔄 [ENRICHING RETRY] eventId=%s, claimId=%s - Attempt %d/%d", 
                            claim.eventId(), claim.claimId(), attempt, MAX_ATTEMPTS);
                }
                callExternalService(claim);
                Log.infof("✨ [ENRICHED] eventId=%s, claimId=%s", 
                        claim.eventId(), claim.claimId());
                return claim;
            } catch (RuntimeException ex) {
                if (attempt >= MAX_ATTEMPTS) {
                    Log.errorf("❌ [ENRICHMENT FAILED] eventId=%s, claimId=%s - Max attempts reached, routing to DLQ", 
                            claim.eventId(), claim.claimId());
                    throw ex; // routed to DLQ
                }
                Log.warnf("⚠️  [ENRICHMENT RETRY] eventId=%s, claimId=%s - External service timeout, retrying...", 
                        claim.eventId(), claim.claimId());
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