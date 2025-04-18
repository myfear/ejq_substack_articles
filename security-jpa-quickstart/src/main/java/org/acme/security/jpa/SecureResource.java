package org.acme.security.jpa;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/api")
public class SecureResource {

    @GET
    @Path("/public")
    public String publicEndpoint() {
        return "This is public.";
    }

    @GET
    @Path("/user")
    @RolesAllowed("user")
    public String userEndpoint() {
        return "Hello, user!";
    }

    @GET
    @Path("/admin")
    @RolesAllowed("admin")
    public String adminEndpoint() {
        return "Hello, admin!";
    }
}
