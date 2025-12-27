package com.mainthread.apikey.admin.dto;

import java.time.Instant;
import java.util.Set;

public record CreateKeyResponse(
        String keyId,
        String apiKey,
        Set<String> features,
        Instant createdAt) {
}