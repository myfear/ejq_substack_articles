package org.acme.model;

public record CardData(
        String recipient,
        String message,
        String sender,
        String year) {
}