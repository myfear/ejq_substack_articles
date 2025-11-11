package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class UserServiceTest {

    @Inject
    UserService userService;

    @Test
    void validId_returnsUser() {
        var user = userService.getUserName(1L);
        assertNotNull(user);
        assertEquals("Alice", user);
    }

    @Test
    void nullId_returnsNull() {
        assertNull(userService.getUserName(null));
    }

    @Test
    void invalidId_returnsEmptyEmail() {
        assertTrue(userService.getUserEmail(99L).isEmpty());
    }
}