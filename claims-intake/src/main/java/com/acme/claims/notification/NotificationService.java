package com.acme.claims.notification;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.acme.claims.events.ClaimAccepted;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationService {

    @Incoming("claims-accepted-in")
    public void notify(ClaimAccepted claim) {
        Log.infof(
                "Claim accepted: " + claim.claimId());
    }
}
