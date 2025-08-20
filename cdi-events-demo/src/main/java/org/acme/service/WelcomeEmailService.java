package org.acme.service;

import org.acme.events.UserRegistrationEvent;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class WelcomeEmailService {

    public void sendWelcome(@Observes UserRegistrationEvent event) {
        Log.infof("WelcomeEmailService: Sending welcome email to " + event.username());
    }
}