package com.example.pii;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/test")
public class TestResource {
    @GET
    @Path("/error")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerError(@QueryParam("fail") boolean fail,
            @QueryParam("name") String name,
            @QueryParam("email") String email) {
        if (fail) {
            throw new MyCustomApplicationException(
                    "Operation failed for user %s (contact: %s). Internal check failed.".formatted(name, email));
        }
        return Response.ok("Operation successful!").build();
    }
}