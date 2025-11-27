package com.example.api.v2;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentV2(
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String method) {
}