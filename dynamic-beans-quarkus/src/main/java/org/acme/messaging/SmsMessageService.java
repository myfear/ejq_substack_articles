package org.acme.messaging;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@IfBuildProperty(name = "app.message-type", stringValue = "sms")
public class SmsMessageService implements MessageService {
    @Override
    public String send(String recipient, String text) {
        return String.format("SMS -> %s :: %s", recipient, text);
    }
}