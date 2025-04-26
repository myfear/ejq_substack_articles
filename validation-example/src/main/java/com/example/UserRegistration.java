package com.example;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.FormParam;

public class UserRegistration {

    @FormParam("username")
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be 4 to 20 characters")
    private String username;

    @FormParam("email")
    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    @FormParam("phone")
    @Pattern(
        regexp = "^\\(\\d{3}\\) \\d{3}-\\d{4}$", 
        message = "Phone must match (123) 456-7890 format"
    )
    private String phone; // Getters and setters
}
