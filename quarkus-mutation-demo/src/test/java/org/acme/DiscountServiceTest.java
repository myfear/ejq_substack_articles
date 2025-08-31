package org.acme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DiscountServiceTest {

    private final DiscountService svc = new DiscountService();

    @Test
    void zeroDiscountUnder100Points() {
        BigDecimal out = svc.applyDiscount(new BigDecimal("100.00"), 50);
        assertEquals(new BigDecimal("100.00"), out);
    }

    @Test
    void fivePercentAt200Points() {
        BigDecimal out = svc.applyDiscount(new BigDecimal("200.00"), 200);
        assertEquals(new BigDecimal("190.00"), out);
    }

    @Test
    void tenPercentAt700Points() {
        BigDecimal out = svc.applyDiscount(new BigDecimal("40.00"), 700);
        assertEquals(new BigDecimal("36.00"), out);
    }

    @Test
    void fifteenPercentAt1200Points() {
        BigDecimal out = svc.applyDiscount(new BigDecimal("100.00"), 1200);
        assertEquals(new BigDecimal("85.00"), out);
    }

    @Test
    void validateInputs() {
        assertThrows(IllegalArgumentException.class, () -> svc.applyDiscount(null, 0));
        assertThrows(IllegalArgumentException.class, () -> svc.applyDiscount(new BigDecimal("-0.01"), 0));
    }

    // MISSING: explicit boundary tests at 100, 500, 1000
}
