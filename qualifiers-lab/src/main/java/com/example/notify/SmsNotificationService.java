package com.example.notify;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@SmsQualifier
public class SmsNotificationService implements NotificationService {

    @Override
    public String send(String message) {
        // In real life: call your SMS gateway
        return "SMS sent: " + message;
    }
}