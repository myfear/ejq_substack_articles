package org.acme.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Embeddable;

@Embeddable
@DiscriminatorColumn(name = "type")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AmountDiscountCoupon.class, name = "amount"),
        @JsonSubTypes.Type(value = PercentageDiscountCoupon.class, name = "percentage")
})
public abstract class DiscountCoupon implements Serializable {

    private String name;

    public DiscountCoupon() {
    }

    public DiscountCoupon(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
