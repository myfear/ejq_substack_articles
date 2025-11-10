package com.coffeeshop.service;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.coffeeshop.model.User;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserService {

    private static final List<User> USERS = List.of(
            new User(1L, "Alice", "alice@example.com"),
            new User(2L, "Bob", "bob@example.com"));

    public @Nullable User findByEmail(String email) {
        return USERS.stream()
                .filter(u -> u.email().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null); // returns null
    }
}