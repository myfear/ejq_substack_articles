package com.acme.claims.processing;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.acme.claims.events.ClaimSubmitted;
import com.acme.claims.events.ClaimValidated;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClaimValidator {

        @Incoming("submitted")
        @Outgoing("claims-validated")
        public ClaimValidated validate(ClaimSubmitted claim) {

                if (claim.amount() <= 0) {
                        Log.errorf("❌ [VALIDATION FAILED] eventId=%s, claimId=%s - Amount must be positive (amount=%.2f)",
                                        claim.eventId(), claim.claimId(), claim.amount());
                        throw new IllegalArgumentException(
                                        "Claim amount must be positive");
                }

                Log.infof("✅ [VALIDATED] eventId=%s, claimId=%s, amount=%.2f",
                                claim.eventId(), claim.claimId(), claim.amount());

                return new ClaimValidated(
                                claim.eventId(),
                                claim.claimId(),
                                claim.customerId(),
                                claim.amount());
        }
}