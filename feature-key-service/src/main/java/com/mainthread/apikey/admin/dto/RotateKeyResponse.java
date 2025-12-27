package com.mainthread.apikey.admin.dto;

import java.time.Instant;

public record RotateKeyResponse(
        String keyId,
        String newApiKey,
        Instant rotatedAt) {
}