package com.example.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
@UserDefinition
public class AppUser extends PanacheEntity {

    @Username
    @Column(unique = true, nullable = false)
    public String email;

    @Password
    @Column(nullable = false)
    public String password; // bcrypt hash

    @Roles
    @Column(nullable = false)
    public String roles; // comma-separated, e.g. “user”

    public static AppUser create(String email, String rawPassword) {
        AppUser user = new AppUser();
        user.email = email;
        user.password = BcryptUtil.bcryptHash(rawPassword);
        user.roles = "user";
        return user;
    }
}
