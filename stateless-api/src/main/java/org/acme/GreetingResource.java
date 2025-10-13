package org.acme;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
public class GreetingResource {

    @Inject
    JsonWebToken jwt;

    @GET
    @RolesAllowed("user")
    public String hello() {
        return "{\"message\":\"Hello " + jwt.getName() + "!\"}";
    }
}