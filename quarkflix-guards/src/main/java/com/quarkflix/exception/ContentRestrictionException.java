package com.quarkflix.exception;

public class ContentRestrictionException extends RuntimeException {
    public ContentRestrictionException(String message) {
        super(message);
    }
}