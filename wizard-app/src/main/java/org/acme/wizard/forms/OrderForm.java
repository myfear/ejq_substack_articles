package org.acme.wizard.forms;

import java.io.Serializable;

import org.jboss.resteasy.reactive.RestForm;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class OrderForm implements Serializable {
    @RestForm
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    public Integer quantity;

    @RestForm
    @NotBlank(message = "Product name is required")
    public String productName;

    // Getters and setters
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}