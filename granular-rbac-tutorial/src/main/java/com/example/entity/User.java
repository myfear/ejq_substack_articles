package com.example.entity;

import org.mindrot.jbcrypt.BCrypt;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users") // “user” is reserved in Postgres
public class User extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    public String password;

    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public static User add(String username, String rawPassword) {
        User u = new User();
        u.username = username;
        u.password = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        u.persist();
        return u;
    }
}