package com.example.memory.api;

import com.example.memory.service.JPAMemoryBot;
import com.example.memory.store.jpa.JPAChatMemoryStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class BotPlaygroundResource {

    @Inject
    JPAMemoryBot jpaMemory;

    @Inject
    JPAChatMemoryStore jpaChatMemoryStore;

    @Inject
    ObjectMapper objectMapper;

    private String id(String bot, String session) {
        return bot + ":" + session;
    }

    @POST
    @Path("/chat")
    public String summarizing(
            @QueryParam("session") String session,
            String message) {

        return jpaMemory.chat(id("summary", session), message);
    }

    @GET
    @Path("/debug")
    @Produces(MediaType.APPLICATION_JSON)
    public String debug(
            @QueryParam("session") String session,
            @QueryParam("bot") String bot) {
        try {
            // Default to "summary" if bot not specified
            String botType = (bot != null && !bot.isEmpty()) ? bot : "summary";
            String memoryId = id(botType, session);

            var messages = jpaChatMemoryStore.getMessages(memoryId);

            // Return pretty-printed JSON
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(messages);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}