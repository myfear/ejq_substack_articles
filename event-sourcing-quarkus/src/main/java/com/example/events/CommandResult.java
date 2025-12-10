package com.example.events;

import java.util.UUID;

public sealed interface CommandResult
        permits CommandResult.Success,
        CommandResult.InvalidState,
        CommandResult.NotFound,
        CommandResult.ValidationError {

    record Success(UUID aggregateId) implements CommandResult {
    }

    record InvalidState(String message) implements CommandResult {
    }

    record NotFound(String message) implements CommandResult {
    }

    record ValidationError(String message) implements CommandResult {
    }
}