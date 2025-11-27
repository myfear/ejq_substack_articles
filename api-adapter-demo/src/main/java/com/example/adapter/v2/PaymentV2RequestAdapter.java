package com.example.adapter.v2;

import java.time.OffsetDateTime;

import com.example.adapter.RequestAdapter;
import com.example.adapter.ResponseAdapter;
import com.example.api.v2.PaymentV2;
import com.example.domain.CanonicalPayment;
import com.example.domain.PaymentStatus;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentV2RequestAdapter implements RequestAdapter<PaymentV2> {
    @Override
    public String version() {
        return "2024-03-15";
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
class PaymentV2ResponseAdapter implements ResponseAdapter<PaymentV2> {
    @Override
    public String version() {
        return "2024-03-15";
    }

    @Override
    public Class<PaymentV2> responseType() {
        return PaymentV2.class;
    }

    @Override
    public PaymentV2 fromCanonical(CanonicalPayment model) {
        return new PaymentV2(model.amount, model.method);
    }
}