package com.acme.claims.processing;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.acme.claims.events.ClaimAccepted;
import com.acme.claims.events.ClaimValidated;
import com.acme.claims.persistence.ProcessedEvent;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ClaimPersister {

        @Incoming("enriched")
        @Outgoing("claims-accepted")
        @Transactional
        public ClaimAccepted persist(ClaimValidated claim) {

                if (ProcessedEvent.alreadyProcessed(claim.eventId())) {
                        Log.warnf("🔄 [DUPLICATE] eventId=%s, claimId=%s - Already processed, ignoring",
                                        claim.eventId(), claim.claimId());
                        return null; // duplicate, safely ignored
                }

                ProcessedEvent.markProcessed(claim.eventId());
                Log.infof("💾 [PERSISTED] eventId=%s, claimId=%s - Marked as processed",
                                claim.eventId(), claim.claimId());

                ClaimAccepted accepted = new ClaimAccepted(
                                claim.eventId(),
                                claim.claimId(),
                                "ACCEPTED");

                Log.infof("✅ [ACCEPTED] eventId=%s, claimId=%s, status=ACCEPTED",
                                accepted.eventId(), accepted.claimId());

                return accepted;
        }
}
