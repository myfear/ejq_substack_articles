package com.example.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public class VehicleAdditionRequest {
    @NotNull
    public String vin;
    @NotNull
    public String makeModel;
    @NotNull
    public Integer riskScore;
    @NotNull
    public LocalDate effectiveFrom;
    public String usageProfile;
}