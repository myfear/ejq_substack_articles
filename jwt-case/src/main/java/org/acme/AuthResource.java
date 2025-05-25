package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthResource {
    @Inject TokenService tokenService;

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(@QueryParam("username") String user, @QueryParam("role") String role) {
        if (user == null || role == null) return Response.status(400).entity("Missing params").build();
        return Response.ok(tokenService.generateToken(user, role)).build();
    }
}
