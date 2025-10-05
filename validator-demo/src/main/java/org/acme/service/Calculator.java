package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.Min;

@ApplicationScoped
public class Calculator {

    public int multiply(@Min(1) int a, @Min(1) int b) {
        return a * b;
    }
}