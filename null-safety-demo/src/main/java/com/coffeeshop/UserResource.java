package com.coffeeshop;

import org.jspecify.annotations.Nullable;

import com.coffeeshop.model.User;
import com.coffeeshop.service.UserService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;

    @GET
    @Path("/by-email/{email}")
    public @Nullable String getUser(@PathParam("email") String email) {
        @Nullable User user = userService.findByEmail(email);
        if (user == null) {
            return null;
        }
        return user.name();
    }
}