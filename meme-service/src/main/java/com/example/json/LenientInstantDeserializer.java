package com.example.json;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class LenientInstantDeserializer extends StdDeserializer<Instant> {

    public LenientInstantDeserializer() {
        super(Instant.class);
    }

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String v = p.getValueAsString();
        if (v == null || v.isBlank())
            return null;

        // Try epoch millis
        if (v.chars().allMatch(Character::isDigit)) {
            try {
                long epoch = Long.parseLong(v);
                return Instant.ofEpochMilli(epoch);
            } catch (NumberFormatException ignored) {
            }
        }

        // Try RFC3339/ISO_INSTANT
        try {
            return Instant.parse(v);
        } catch (Exception ignored) {
        }

        // Try yyyy-MM-dd as local date at midnight UTC
        try {
            LocalDate d = LocalDate.parse(v, DateTimeFormatter.ISO_LOCAL_DATE);
            return d.atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (Exception ignored) {
        }

        // Last resort: throw a meaningful error
        throw new IOException("Unsupported date format for createdAt: " + v);
    }
}