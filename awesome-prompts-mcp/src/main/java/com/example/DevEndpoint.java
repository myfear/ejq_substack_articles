package com.example;

import com.example.service.PromptRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/dev")
@Produces(MediaType.TEXT_PLAIN)
public class DevEndpoint {
    @Inject
    PromptRepository repo;

    @GET
    @Path("/count")
    public String count() {
        return "Prompts loaded: " + repo.all().size();
    }

    @POST
    @Path("/refresh")
    public String refresh() {
        repo.refreshPrompts();
        return "Refreshed";
    }
}