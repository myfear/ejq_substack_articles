package com.acme.totp.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserStore {
    public static class User {
        public final String username;
        public final String bcryptHash; // demo only
        public final String base32Secret; // TOTP seed in Base32

        public User(String username, String bcryptHash, String base32Secret) {
            this.username = username;
            this.bcryptHash = bcryptHash;
            this.base32Secret = base32Secret;
        }
    }

    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    public static void put(User u) {
        USERS.put(u.username, u);
    }

    public static User get(String username) {
        return USERS.get(username);
    }

    public static boolean exists(String username) {
        return USERS.containsKey(username);
    }
}