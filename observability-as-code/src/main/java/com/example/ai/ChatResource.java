package com.example.ai;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/ai")
public class ChatResource {

    private final ChatService chatService;

    public ChatResource(ChatService chatService) {
        this.chatService = chatService;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String chat(@QueryParam("q") String prompt) {
        return chatService.ask(prompt != null ? prompt : "Hello from Quarkus!");
    }
}