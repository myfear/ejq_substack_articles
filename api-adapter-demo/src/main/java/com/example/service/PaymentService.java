package com.example.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.domain.CanonicalPayment;
import com.example.domain.PaymentStatus;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentService {

    public CanonicalPayment create(CanonicalPayment req) {
        // In real life: fraud checks, ledger writes, async webhooks, etc.
        String id = UUID.randomUUID().toString();
        return new CanonicalPayment(
                id,
                req.amount,
                req.method == null ? "CARD" : req.method,
                PaymentStatus.AUTHORIZED,
                OffsetDateTime.now());
    }
}