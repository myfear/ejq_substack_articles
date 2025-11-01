package org.acme.messaging;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProperty(name = "app.message-type", stringValue = "email")
public class EmailMessageService implements MessageService {
    @Override
    public String send(String recipient, String text) {
        return String.format("EMAIL -> %s :: %s", recipient, text);
    }
}