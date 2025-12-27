package com.mainthread.apikey.admin.dto;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreateKeyRequest(
        @NotEmpty @Size(max = 20) Set<@Size(min = 3, max = 80) String> features) {
}