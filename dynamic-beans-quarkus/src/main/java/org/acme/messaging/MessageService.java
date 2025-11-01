package org.acme.messaging;

public interface MessageService {
    String send(String recipient, String text);
}