package com.example;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class InsufficientFundsException extends WebApplicationException {

    public InsufficientFundsException(String message) {
        super(message, Response.Status.BAD_REQUEST);
    }
}