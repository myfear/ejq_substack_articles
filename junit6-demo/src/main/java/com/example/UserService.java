package com.example;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserService {

    public String getUserName(Long id) {
        if (id == null)
            return null;
        return id == 1L ? "Alice" : null;
    }

    public Optional<String> getUserEmail(Long id) {
        if (id == null || id != 1L)
            return Optional.empty();
        return Optional.of("alice@example.com");
    }
}