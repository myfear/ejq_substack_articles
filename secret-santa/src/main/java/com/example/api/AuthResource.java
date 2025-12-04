package com.example.api;

import com.example.security.AppUser;

import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    public static class RegisterRequest {
        public String email;
        public String password;
    }

    @POST
    @Path("/register")
    @PermitAll
    @Transactional
    public Response register(RegisterRequest request) {
        if (request.email == null || request.password == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (AppUser.find("email", request.email).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("User already exists").build();
        }

        AppUser user = AppUser.create(request.email, request.password);
        user.persist();

        return Response.status(Response.Status.CREATED).build();
    }
}