package com.ibm.api;

import java.util.Set;

import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/dev/token")
public class DevTokenResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDevToken(@QueryParam("user") String user) {
        return Jwt.issuer("https://example.com/issuer")
                .upn(user)
                .groups(Set.of("User"))
                .sign();
    }
}