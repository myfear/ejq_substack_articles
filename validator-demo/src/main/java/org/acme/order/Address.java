package org.acme.order;

import org.acme.validation.ValidationGroups.OnCreate;

import jakarta.validation.constraints.NotBlank;

public class Address {
    @NotBlank(groups = OnCreate.class)
    private String street;
    @NotBlank(groups = OnCreate.class)
    private String city;

    // Default constructor
    public Address() {}

    // Getters and setters
    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
