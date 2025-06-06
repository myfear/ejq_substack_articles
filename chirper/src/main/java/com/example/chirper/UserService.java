package com.example.chirper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    @Transactional
    public User createUser(String username, String displayName, String bio) {
        var user = new User();
        user.username = username;
        user.displayName = displayName;
        user.bio = bio;
        user.persist();
        return user;
    }

    public User findByUsername(String username) {
        return User.findByUsername(username);
    }

    @Transactional
    public User getOrCreateUser(String username) {
        var user = findByUsername(username);
        return user != null ? user : createUser(username, username, "New Chirper user!");
    }
}
