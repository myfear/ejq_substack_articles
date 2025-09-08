package com.example.orders.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class OrderEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String customer;

    @Column(nullable = false)
    private String item;

    private int quantity;

    public OrderEntity() {
    }

    public OrderEntity(String customer, String item, int quantity) {
        this.customer = customer;
        this.item = item;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public String getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}