package org.acme.messaging;

public class DynamicSmsService implements DynamicMessageService {
    @Override
    public String send(String recipient, String text) {
        return String.format("SMS -> %s :: %s", recipient, text);
    }
}
