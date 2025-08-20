package org.acme.service;

import org.acme.events.UserRegistrationEvent;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class AuditService {

    public void onUserRegistered(@Observes UserRegistrationEvent event) {
        Log.infof("AuditService: User registered: " + event.username());
    }
    
}