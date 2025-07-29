package org.acme.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public record Session(
        String id,
        String userName,
        PersonalityType personality,
        Scenario scenario,
        List<Message> messages,
        String status,
        Instant createdAt) {
    public Session(String id, String userName, PersonalityType personality, Scenario scenario) {
        this(id, userName, personality, scenario, new ArrayList<>(), "ACTIVE", Instant.now());
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }
}