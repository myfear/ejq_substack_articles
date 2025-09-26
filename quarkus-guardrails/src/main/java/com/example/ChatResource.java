package com.example;

import com.example.ai.ChatResponse;
import com.example.ai.ChatService;

import dev.langchain4j.guardrail.GuardrailException;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/chat")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ChatResource {

    @Inject
    ChatService chatService;

    @POST
    public Response chat(ChatRequest req) {
        try {
            ChatResponse reply = chatService.chat(req.message());
            return Response.ok(reply).build();
        } catch (GuardrailException e) {
            Log.errorf(e, "Error calling the LLM: %s", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("GUARDRAIL_VIOLATION", e.getMessage()))
                    .build();
        } catch (Exception e) {
            Log.errorf(e, "Unexpected error calling the LLM: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"))
                    .build();
        }
    }


    public record ErrorResponse(String error, String message) {
    }

    public record ChatRequest(String message) {
    }


}
