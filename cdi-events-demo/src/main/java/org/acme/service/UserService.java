package org.acme.service;

import org.acme.events.UserRegistrationEvent;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserService {

    @Inject
    Event<UserRegistrationEvent> userRegistered;

    public void registerUser(String username) {
        // pretend to store the user somewhere
        Log.infof("UserService: Registered user " + username);

        // fire the event
        userRegistered.fire(new UserRegistrationEvent(username));
    }
}