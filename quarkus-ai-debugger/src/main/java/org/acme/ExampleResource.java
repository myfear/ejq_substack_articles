package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class ExampleResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello, this endpoint works!";
    }

    @GET
    @Path("/error")
    @Produces(MediaType.TEXT_PLAIN)
    public String causeError() {
        String myObject = null;
        myObject.length(); // Intentional NullPointerException
        return "You will never see this message.";
    }
}