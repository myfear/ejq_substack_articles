package com.example.adapter;

import com.example.domain.CanonicalPayment;

public interface ResponseAdapter<R> {
    String version();

    Class<R> responseType();

    R fromCanonical(CanonicalPayment model);
}