package com.example.user;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/admin")
public class AdminResource {
    @GET
    @RolesAllowed("ADMIN")
    public Response adminOnly() {
        return Response.ok("secret").build();
    }
}