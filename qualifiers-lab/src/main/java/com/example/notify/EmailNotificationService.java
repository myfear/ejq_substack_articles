package com.example.notify;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@EmailQualifier
public class EmailNotificationService implements NotificationService {

    @Override
    public String send(String message) {
        // In real life: hand off to your mailer
        return "EMAIL sent: " + message;
    }
}