package com.example;

import jakarta.validation.constraints.*;
import jakarta.ws.rs.FormParam;

public class UserRegistration {

    @FormParam("username")
    @NotBlank
    @Size(min = 4, max = 20)
    private String username;

    @FormParam("email")
    @NotBlank
    @Email
    private String email;

    @FormParam("phone")
    @Pattern(
        regexp = "^\\(\\d{3}\\) \\d{3}-\\d{4}$"
    )
    private String phone;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
