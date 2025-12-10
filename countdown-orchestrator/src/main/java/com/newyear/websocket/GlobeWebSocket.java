package com.newyear.websocket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.newyear.entity.ScheduledGreeting;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

/**
 * WebSocket endpoint that broadcasts New Year greetings
 * to all connected browser clients.
 *
 * Frontend connects to: ws://localhost:8080/ws/globe
 */
@ServerEndpoint("/ws/globe")
@ApplicationScoped
public class GlobeWebSocket {

    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
    }

    /**
     * Called by the delivery service when a greeting is successfully delivered.
     * Broadcasts a JSON event of type GREETING_DELIVERED.
     */
    public void broadcastGreetingDelivered(ScheduledGreeting greeting) {
        if (greeting.deliveredAt == null) {
            // Should not happen, but be defensive.
            return;
        }

        JsonObject event = Json.createObjectBuilder()
                .add("type", "GREETING_DELIVERED")
                .add("timezone", greeting.recipientTimezone)
                .add("timestamp", greeting.deliveredAt.toString())
                .add("recipientName", greeting.recipientName)
                .add("message", greeting.message == null ? "" : greeting.message)
                .build();

        String payload = event.toString();

        for (Session session : sessions) {
            if (session.isOpen()) {
                session.getAsyncRemote().sendText(payload);
            }
        }
    }
}