package org.acme;

import org.acme.service.UserService;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/users")
public class UserResource {

    @Inject
    UserService userService;

    @POST
    public Response register(String username) {
        userService.registerUser(username);
        return Response.ok("User registered: " + username).build();
    }
}