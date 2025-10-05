package org.acme.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

@QuarkusTest
class CalculatorTest {

    @Inject
    Calculator calculator;

    @Test
    void shouldMultiplyValidInputs() {
        int result = calculator.multiply(3, 4);
        assertEquals(12, result);
    }

    @Test
    void shouldThrowOnInvalidInput() {
        assertThrows(ConstraintViolationException.class,
                () -> calculator.multiply(0, 5));
    }
}