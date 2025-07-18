package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class BankingService {

    private double balance = 100.00;

    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new WebApplicationException("Withdrawal amount must be positive.", 400);
        }
        if (amount == 99.99) {
            throw new RuntimeException("Failed to connect to transaction ledger!");
        }
        if (amount > balance) {
            throw new InsufficientFundsException("You only have $" + balance + " in your account.");
        }
        this.balance -= amount;
    }
}