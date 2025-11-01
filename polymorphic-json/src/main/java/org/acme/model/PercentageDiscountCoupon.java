package org.acme.model;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embeddable;

@Embeddable
@DiscriminatorValue("discount.coupon.percentage")
public class PercentageDiscountCoupon extends DiscountCoupon {

    private BigDecimal percentage;

    public PercentageDiscountCoupon() {
    }

    public PercentageDiscountCoupon(String name) {
        super(name);
    }

    public BigDecimal getPercentage() {
        return percentage;
    }

    public PercentageDiscountCoupon setPercentage(BigDecimal amount) {
        this.percentage = amount;
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        return o instanceof PercentageDiscountCoupon that
                && Objects.equals(percentage, that.percentage);

    }

    @Override
    public int hashCode() {
        return Objects.hashCode(percentage);
    }

}
