package com.acme.claims.notification;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.acme.claims.events.ClaimAccepted;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationService {

    @Incoming("accepted")
    public void notify(ClaimAccepted claim) {
        Log.infof("📧 [NOTIFICATION] eventId=%s, claimId=%s, status=%s - Sending notification to customer",
                claim.eventId(), claim.claimId(), claim.status());
    }
}