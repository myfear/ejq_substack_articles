package org.acme.order;

import org.acme.validation.ValidationGroups.OnCreate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class OrderItem {
    @NotBlank(groups = OnCreate.class)
    private String productCode;
    @Min(value = 1, groups = OnCreate.class)
    private int quantity;

    // Default constructor
    public OrderItem() {}

    // Getters and setters
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
