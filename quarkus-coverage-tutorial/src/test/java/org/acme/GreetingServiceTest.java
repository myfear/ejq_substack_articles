package org.acme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest // This ensures the service can be @Inject'ed
public class GreetingServiceTest {

    @Inject
    GreetingService greetingService;

    @Test
    public void testGreetWithName() {
        assertEquals("Hello, Quarkus!", greetingService.greet("Quarkus"));
    }

    @Test
    public void testGreetNullName() {
        assertEquals("Hello, stranger!", greetingService.greet(null));
    }

    @Test
    public void testGreetEmptyName() {
        assertEquals("Hello, stranger!", greetingService.greet(""));
    }

    // We are intentionally NOT testing the "admin" branch in greet()
    // and NOT testing the farewell() method yet to see them uncovered.
}