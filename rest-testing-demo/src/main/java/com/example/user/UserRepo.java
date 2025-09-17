package com.example.user;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepo {
    private final Map<Long, User> db = new ConcurrentHashMap<>();

    public Optional<User> find(long id) {
        return Optional.ofNullable(db.get(id));
    }

    public User save(long id, UserCreate in) {
        var u = new User(id, in.name(), in.email(), in.role());
        db.put(id, u);
        return u;
    }
}