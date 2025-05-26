package com.example.pii;

public class MyCustomApplicationException extends RuntimeException {
    public MyCustomApplicationException(String message) {
        super(message);
    }
}