package org.acme.messaging;

public interface DynamicMessageService {
    String send(String recipient, String text);
}