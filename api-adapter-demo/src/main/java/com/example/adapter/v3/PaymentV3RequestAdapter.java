package com.example.adapter.v3;

import java.time.OffsetDateTime;

import com.example.adapter.RequestAdapter;
import com.example.adapter.ResponseAdapter;
import com.example.api.v2.PaymentV2; // V3 reuses V2 request but adds V3 response features
import com.example.api.v3.PaymentV3Response;
import com.example.domain.CanonicalPayment;
import com.example.domain.PaymentStatus;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentV3RequestAdapter implements RequestAdapter<PaymentV2> {
    @Override
    public String version() {
        return "2024-09-01";
    }

    @Override
    public Class<PaymentV2> requestType() {
        return PaymentV2.class;
    }

    @Override
    public CanonicalPayment toCanonical(PaymentV2 request) {
        return new CanonicalPayment(
                null,
                request.amount(),
                request.method(),
                PaymentStatus.PENDING,
                OffsetDateTime.now());
    }
}

@ApplicationScoped
class PaymentV3ResponseAdapter implements ResponseAdapter<PaymentV3Response> {
    @Override
    public String version() {
        return "2024-09-01";
    }

    @Override
    public Class<PaymentV3Response> responseType() {
        return PaymentV3Response.class;
    }

    @Override
    public PaymentV3Response fromCanonical(CanonicalPayment model) {
        boolean confirmationRequired = model.status == PaymentStatus.AUTHORIZED;
        return new PaymentV3Response(
                model.id,
                model.amount,
                model.method,
                model.status.name(),
                model.createdAt,
                confirmationRequired);
    }
}