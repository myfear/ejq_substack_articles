package com.ibm.api;

import com.ibm.ai.SalesEnablementBot;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/bot")
public class SalesEnablementResource {

    @Inject
    SalesEnablementBot bot;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public BotResponse ask(@QueryParam("q") String question) {
        if (question == null || question.trim().isEmpty()) {
            question = "What is the best solution for a client who is migrating to a microservices architecture?";
        }
        String botResponse = bot.chat(question);
        return new BotResponse(botResponse);
    }
}