package com.example.adapter.v1;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.example.adapter.RequestAdapter;
import com.example.adapter.ResponseAdapter;
import com.example.api.v1.PaymentV1;
import com.example.domain.CanonicalPayment;
import com.example.domain.PaymentStatus;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentV1RequestAdapter implements RequestAdapter<PaymentV1> {
    @Override
    public String version() {
        return "2023-10-16";
    }

    @Override
    public Class<PaymentV1> requestType() {
        return PaymentV1.class;
    }

    @Override
    public CanonicalPayment toCanonical(PaymentV1 request) {
        return new CanonicalPayment(
                null, // id created by service
                request.amount(),
                "CARD", // default method in V1
                PaymentStatus.PENDING, // initial
                OffsetDateTime.now());
    }
}

@ApplicationScoped
class PaymentV1ResponseAdapter implements ResponseAdapter<PaymentV1> {
    @Override
    public String version() {
        return "2023-10-16";
    }

    @Override
    public Class<PaymentV1> responseType() {
        return PaymentV1.class;
    }

    @Override
    public PaymentV1 fromCanonical(CanonicalPayment model) {
        // V1 only exposes amount back
        BigDecimal amt = model.amount == null ? BigDecimal.ZERO : model.amount;
        return new PaymentV1(amt);
    }
}