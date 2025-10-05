package org.acme.product;

import org.acme.validation.ValidDiscount;

public class Product {
    private String name;

    @ValidDiscount
    private Integer discount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }
}