package com.example.api.v1;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record PaymentV1(
        @NotNull @DecimalMin("0.01") BigDecimal amount) {
}