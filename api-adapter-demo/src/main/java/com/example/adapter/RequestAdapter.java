package com.example.adapter;

import com.example.domain.CanonicalPayment;

public interface RequestAdapter<T> {
    String version(); // ex: “2023-10-16”

    Class<T> requestType(); // ex: PaymentV2.class

    CanonicalPayment toCanonical(T request);
}