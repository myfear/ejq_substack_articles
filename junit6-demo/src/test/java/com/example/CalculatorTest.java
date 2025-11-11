package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class CalculatorTest {

    @Inject
    Calculator calc;

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @CsvSource({
            "1,1,2",
            "2,3,5",
            "-5,5,0"
    })
    @Tag("Calculator")
    void addition(int a, int b, int expected) {
        assertEquals(expected, calc.add(a, b));
    }

    @ParameterizedTest(name = "{0} * {1} = {2}")
    @CsvSource(delimiter = '|', textBlock = """
            2 | 3 | 6
            5 | 4 | 20
            -2 | 3 | -6
            """)
    void multiplication(int a, int b, int expected) {
        assertEquals(expected, calc.multiply(a, b));
    }

    @ParameterizedTest
    @org.junit.jupiter.params.provider.CsvFileSource(resources = "/division.csv", numLinesToSkip = 1)
    void divisionFromFile(int dividend, int divisor, double expected) {
        assertEquals(expected, calc.divide(dividend, divisor), 0.001);
    }

}