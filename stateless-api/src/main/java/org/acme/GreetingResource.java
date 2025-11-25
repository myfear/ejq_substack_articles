package org.acme;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST resource that provides a greeting endpoint.
 * This resource requires JWT authentication and the "user" role.
 */
@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
public class GreetingResource {

    @Inject
    JsonWebToken jwt;

    /**
     * Returns a personalized greeting message for the authenticated user.
     * 
     * This endpoint requires the user to be authenticated with a valid JWT token
     * and have the "user" role. The greeting includes the user's name from the JWT.
     * 
     * @return a JSON string containing a personalized greeting message
     */
    @GET
    @RolesAllowed("user")
    public String hello() {
        return "{\"message\":\"Hello " + jwt.getName() + "!\"}";
    }
}