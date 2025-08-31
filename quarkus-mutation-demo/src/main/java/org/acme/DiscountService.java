package org.acme;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Applies a discount based on loyalty points:
 * - < 100 points: 0%
 * - 100..499 points: 5%
 * - 500..999 points: 10%
 * - >= 1000 points: 15%
 *
 * Business rule: discount applies to subtotal only. Never returns negative
 * numbers.
 */
public class DiscountService {

    public BigDecimal applyDiscount(BigDecimal subtotal, int loyaltyPoints) {
        if (subtotal == null)
            throw new IllegalArgumentException("subtotal is required");
        if (subtotal.signum() < 0)
            throw new IllegalArgumentException("subtotal must be >= 0");

        BigDecimal rate = discountRate(loyaltyPoints);
        BigDecimal discounted = subtotal.multiply(BigDecimal.ONE.subtract(rate));

        // Round to cents using banker’s rounding
        return discounted.setScale(2, RoundingMode.HALF_EVEN);
    }

    BigDecimal discountRate(int loyaltyPoints) {
        if (loyaltyPoints < 100)
            return BigDecimal.ZERO;
        if (loyaltyPoints < 500)
            return bd("0.05");
        if (loyaltyPoints < 1000)
            return bd("0.10");
        return bd("0.15");
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
}