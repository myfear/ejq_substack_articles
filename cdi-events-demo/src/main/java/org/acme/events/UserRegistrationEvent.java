package org.acme.events;

public class UserRegistrationEvent {
    private final String username;

    public UserRegistrationEvent(String username) {
        this.username = username;
    }

    public String username() {
        return username;
    }
}