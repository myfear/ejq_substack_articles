package org.acme.model;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embeddable;

@Embeddable
@DiscriminatorValue("discount.coupon.amount")
public class AmountDiscountCoupon extends DiscountCoupon {

    private BigDecimal amount;

    public AmountDiscountCoupon() {
    }

    public AmountDiscountCoupon(String name) {
        super(name);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public AmountDiscountCoupon setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof AmountDiscountCoupon that
                && Objects.equals(amount, that.amount);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount);
    }
}
