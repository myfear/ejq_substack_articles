package com.acme;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.quarkus.logging.Log;

@ApplicationScoped
public class NotificationSink {

    @Incoming("totals")
    public void notifyTotal(Double total) {
        Log.infof("Tap closed. Total value of all prepared orders: %s", total);
    }
}