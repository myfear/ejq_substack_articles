package org.acme;

import org.acme.errorhandling.MyCustomApplicationException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class GreetingResource {

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus";
    }

    @GET
    @Path("/error")
    @Produces(MediaType.TEXT_HTML)
    public String cause500() {
        throw new RuntimeException("Simulated failure!");
    }

    @GET
    @Path("/api/error")
    @Produces(MediaType.APPLICATION_JSON)
    public String apiError() {
        throw new RuntimeException("Simulated API error!");
    }

    @GET
    @Path("/custom-error")
    @Produces(MediaType.APPLICATION_JSON)
    public String triggerCustomError() {
        throw new MyCustomApplicationException("This is a test of the custom application exception.");
    }

}
