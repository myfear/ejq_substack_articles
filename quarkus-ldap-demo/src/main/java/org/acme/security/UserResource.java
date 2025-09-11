package org.acme.security;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/users")
public class UserResource {
    
    @GET
    @Path("/me")
    @RolesAllowed("standardRole")
    public String me(@Context SecurityContext sc) {
        return sc.getUserPrincipal().getName();
    }
    
    @GET
    @Path("/public")
    public String publicEndpoint(@Context SecurityContext sc) {
        return "Hello " + (sc.getUserPrincipal() != null ? sc.getUserPrincipal().getName() : "anonymous") + "! This is a public endpoint.";
    }
}