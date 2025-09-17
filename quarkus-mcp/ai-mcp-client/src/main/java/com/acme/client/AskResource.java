package com.acme.client;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/ask")
@Produces(MediaType.TEXT_PLAIN)
public class AskResource {

    @Inject
    WeatherAssistant assistant;

    @GET
    public String ask(@QueryParam("q") String q) {
        return assistant.chat(q == null ? "What is the temperature in Berlin?" : q);
    }
}