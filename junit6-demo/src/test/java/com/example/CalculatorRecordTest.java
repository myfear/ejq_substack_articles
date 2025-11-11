package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

record CalculatorRecordTest() {

    @Test
    @Tag("Calculator")
    void addsCorrectly() {
        var calc = new Calculator();
        assertEquals(4, calc.add(2, 2));
    }

    @Test
    @Tag("Calculator")
    void multipliesCorrectly() {
        var calc = new Calculator();
        assertEquals(6, calc.multiply(2, 3));
    }
}