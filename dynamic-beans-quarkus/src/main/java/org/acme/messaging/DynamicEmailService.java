package org.acme.messaging;

public class DynamicEmailService implements DynamicMessageService {
    @Override
    public String send(String recipient, String text) {
        return String.format("EMAIL -> %s :: %s", recipient, text);
    }
}