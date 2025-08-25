package com.example.versioning.exceptions;

public class UnsupportedVersionException extends RuntimeException {
    public UnsupportedVersionException(String message) {
        super(message);
    }
}