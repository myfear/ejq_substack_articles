package org.acme.domain;

import java.time.Instant;

public record Product(String id, String name, int stock, Instant lastUpdated) {
}