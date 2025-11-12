package com.example.model;

import jakarta.persistence.EnumeratedValue;

public enum PostStatus {

    PENDING(100),
    APPROVED(10),
    SPAM(50),
    REQUIRES_MODERATOR_INTERVENTION(1);

    @EnumeratedValue
    private final int statusCode;

    PostStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}