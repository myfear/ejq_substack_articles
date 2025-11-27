package com.example.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CanonicalPayment {
    public String id;
    public BigDecimal amount;
    public String method; // CARD, SEPA, PAYPAL, etc.
    public PaymentStatus status;
    public OffsetDateTime createdAt;

    public CanonicalPayment() {
    }

    public CanonicalPayment(String id, BigDecimal amount, String method,
            PaymentStatus status, OffsetDateTime createdAt) {
        this.id = id;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.createdAt = createdAt;
    }
}