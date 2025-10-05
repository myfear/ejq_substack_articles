package org.acme.order;

import org.acme.validation.ValidationGroups.OnCreate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Order {
    @NotNull(groups = OnCreate.class)
    @Valid
    private Customer customer;
    @Valid
    private Address address;
    @NotNull(groups = OnCreate.class)
    @Valid
    private java.util.List<OrderItem> items;
    @NotBlank(groups = OnCreate.class)
    private String orderNumber;

    // Default constructor
    public Order() {
    }

    // Getters and setters
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public java.util.List<OrderItem> getItems() {
        return items;
    }

    public void setItems(java.util.List<OrderItem> items) {
        this.items = items;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}