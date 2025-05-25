package com.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    private final GreetingService greetingService;

    public GreetingResource(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String helloProgrammatic() {
        return greetingService.getGreeting();
    }

    @GET
    @Path("/annotated-status")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloAnnotatedStatus() {
        return greetingService.getAnnotatedGreetingStatus();
    }

    @GET
    @Path("/conditional-endpoint")
    @Produces(MediaType.TEXT_PLAIN)
    public String conditionalEndpoint() {
        return greetingService.getGreeting();
    }

}
