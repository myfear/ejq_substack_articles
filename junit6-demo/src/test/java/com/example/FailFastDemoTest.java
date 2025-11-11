package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FailFastDemoTest {

    @Test
    @Order(1)
    void success() {
        assertEquals(2, 1 + 1);
    }

    @Test
    @Order(2)
    void failsAndStops() {
        fail("This failure triggers fail-fast mode");
    }

    @Test
    @Order(3)
    void willNotRun() {
        System.out.println("Should not execute");
    }
}
