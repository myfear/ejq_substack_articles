package com.example.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreate(
        @NotBlank String name,
        @Email String email,
        @NotBlank String role) {
}
