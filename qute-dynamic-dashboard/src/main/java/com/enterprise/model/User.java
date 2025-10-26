package com.enterprise.model;

public class User {
    private String username;
    private String role;
    private String email;

    public User(String username, String role, String email) {
        this.username = username;
        this.role = role;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }
}