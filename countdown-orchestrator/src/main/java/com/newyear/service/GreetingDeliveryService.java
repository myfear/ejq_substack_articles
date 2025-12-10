package com.newyear.service;

import java.time.Instant;

import com.newyear.entity.ScheduledGreeting;
import com.newyear.websocket.GlobeWebSocket;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Contains the actual delivery logic for a greeting.
 * In a real system this would send an email, SMS, push notification, etc.
 */
@ApplicationScoped
public class GreetingDeliveryService {

    @Inject
    GlobeWebSocket wsNotifier;

    @Transactional
    public void deliver(Long greetingId) {
        ScheduledGreeting greeting = ScheduledGreeting.findById(greetingId);

        if (greeting == null) {
            System.out.println("No greeting found for id " + greetingId);
            return;
        }

        if (greeting.delivered) {
            // Idempotency: do not deliver twice.
            System.out.println("Greeting " + greetingId + " already delivered, skipping.");
            return;
        }

        System.out.println("🎉 DELIVERING GREETING TO: " + greeting.recipientName
                + " in " + greeting.recipientTimezone);

        // 1. Simulate external delivery (email/SMS/etc.)
        // In production, inject and use a real Mailer or external API client.
        if (greeting.deliveryChannel != null) {
            System.out.println("Pretending to send via channel: " + greeting.deliveryChannel
                    + " to " + greeting.contactInfo);
        }

        // 2. Mark as delivered in the database
        greeting.delivered = true;
        greeting.deliveredAt = Instant.now();
        // No need to call persist() again; entity is managed in this transaction.

        // 3. Notify all connected WebSocket clients
        wsNotifier.broadcastGreetingDelivered(greeting);
    }
}