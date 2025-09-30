package com.example.api.v2.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class OrderRequestV2 {

    @NotBlank
    public String product;

    @Positive
    public int quantity;

    @NotBlank
    public String customerId;
}