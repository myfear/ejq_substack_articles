package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserService {

    public int getCurrentUserDiscountLimit() {
        return 20; // in real life, check DB or security context
    }
}