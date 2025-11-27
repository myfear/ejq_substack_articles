package com.example.version;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class VersionContext {
    private String version;

    public String get() {
        return version;
    }

    public void set(String version) {
        this.version = version;
    }
}