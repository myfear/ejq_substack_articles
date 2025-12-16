package com.example.ooo.api;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OooRequest(
        @NotBlank String displayName,
        @Email @NotBlank String email,
        @NotNull @Future LocalDate returnDate,
        @NotBlank String locale,
        @NotNull Tone tone,
        String backupContactName,
        @Email String backupContactEmail) {
    public enum Tone {
        FORMAL,
        FRIENDLY,
        DRY,
        FUNNY
    }
}