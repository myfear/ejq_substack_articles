package org.acme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class DiscountServiceBoundaryTest {

    private final DiscountService svc = new DiscountService();

    @Test
    void exactly100PointsGetsFivePercent() {
        BigDecimal out = svc.applyDiscount(new BigDecimal("100.00"), 100);
        assertEquals(new BigDecimal("95.00"), out);
    }

    @Test
    void exactly500PointsGetsTenPercent() {
        BigDecimal out = svc.applyDiscount(new BigDecimal("100.00"), 500);
        assertEquals(new BigDecimal("90.00"), out);
    }

    @Test
    void exactly1000PointsGetsFifteenPercent() {
        BigDecimal out = svc.applyDiscount(new BigDecimal("100.00"), 1000);
        assertEquals(new BigDecimal("85.00"), out);
    }
}
