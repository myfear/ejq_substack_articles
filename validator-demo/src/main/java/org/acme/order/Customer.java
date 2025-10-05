package org.acme.order;

import org.acme.validation.ValidationGroups.OnCreate;

import jakarta.validation.constraints.NotBlank;

public class Customer {
    @NotBlank(groups = OnCreate.class)
    private String name;
    @NotBlank(groups = OnCreate.class)
    private String email;

    // Default constructor
    public Customer() {}

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
