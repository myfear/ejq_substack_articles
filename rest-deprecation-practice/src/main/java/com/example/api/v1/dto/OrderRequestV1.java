package com.example.api.v1.dto;

import jakarta.validation.constraints.NotBlank;

public class OrderRequestV1 {

    @NotBlank
    public String product;

    public int quantity;
}