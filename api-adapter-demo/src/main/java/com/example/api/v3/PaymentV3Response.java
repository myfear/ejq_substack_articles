package com.example.api.v3;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * V3 returns more metadata and async confirmation hints.
 */
public record PaymentV3Response(
        String id,
        BigDecimal amount,
        String method,
        String status,
        OffsetDateTime createdAt,
        boolean confirmationRequired) {
}