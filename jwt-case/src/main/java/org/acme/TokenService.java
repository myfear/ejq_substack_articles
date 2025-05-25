package org.acme;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenService {
    public String generateToken(String username, String... roles) {
        return Jwt.issuer("https://quarkus.io/jwt-case")
                  .upn(username)
                  .groups(new HashSet<>(Arrays.asList(roles)))
                  .expiresIn(Duration.ofHours(1))
                  .sign();
    }
}
