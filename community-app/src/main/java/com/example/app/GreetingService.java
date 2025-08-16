package com.example.app;

import org.springframework.stereotype.Service;

@Service
public class GreetingService {
    public String greet(String name) {
        return "Hello, " + (name == null || name.isBlank() ? "world" : name) + "!";
    }
}