package org.acme.model;

import java.time.Instant;

public record Message(String sender, String content, Instant timestamp) {
    public Message(String sender, String content) {
        this(sender, content, Instant.now());
    }
}