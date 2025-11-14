package com.example.memory.api;

import com.example.memory.aiservices.CompressedMemoryBot;
import com.example.memory.aiservices.JPAMemoryBot;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/bots")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class BotPlaygroundResource {

    @Inject
    CompressedMemoryBot compressed;

    @Inject
    JPAMemoryBot jpaMemory;

    private String id(String bot, String session) {
        return bot + ":" + session;
    }

    @POST
    @Path("/fixed")
    public String fixed(
            @QueryParam("session") String session,
            String message) {
        return compressed.chat(id("fixed", session), message);
    }



    @POST
    @Path("/summarizing")
    public String summarizing(
            @QueryParam("session") String session,
            String message) {

        return jpaMemory.chat(id("summary", session), message);
    }
}