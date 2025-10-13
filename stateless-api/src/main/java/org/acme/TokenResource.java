package org.acme;

import java.time.Instant;
import java.util.Set;

import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/token")
@Produces(MediaType.TEXT_PLAIN)
public class TokenResource {

    @GET
    public String issueToken() {
        return Jwt.issuer("https://the-main-thread.com")
                .upn("duke@example.com")
                .groups(Set.of("user"))
                .expiresAt(Instant.now().plusSeconds(3600))
                .sign();
    }
}