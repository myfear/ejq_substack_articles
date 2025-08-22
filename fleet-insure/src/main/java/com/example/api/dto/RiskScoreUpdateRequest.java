package com.example.api.dto;

import jakarta.validation.constraints.NotNull;

public class RiskScoreUpdateRequest {
    @NotNull
    public Integer newRiskScore;
}