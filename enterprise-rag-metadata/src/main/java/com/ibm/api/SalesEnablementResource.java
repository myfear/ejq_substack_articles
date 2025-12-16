package com.ibm.api;

import com.ibm.ai.SalesEnablementBot;

import io.quarkus.logging.Log;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/bot")
@Authenticated
public class SalesEnablementResource {

    public record ChatResponse(String answer) {
    }

    @Inject
    SalesEnablementBot bot;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ChatResponse ask(@QueryParam("q") String question) {
        Log.infof("Question: %s", question);
        if (question == null || question.trim().isEmpty()) {
            question = "What is the best solution for a client who is migrating to a microservices architecture?";
        }
        String answer = bot.chat(question);

        // Log the raw LLM response for debugging
        Log.infof("=== RAW LLM RESPONSE START ===");
        Log.infof("Response type: %s", answer != null ? answer.getClass().getName() : "null");
        Log.infof("Response length: %d characters", answer != null ? answer.length() : 0);
        Log.infof("Response content: %s", answer);
        Log.infof("=== RAW LLM RESPONSE END ===");

        return new ChatResponse(answer);
    }

}
