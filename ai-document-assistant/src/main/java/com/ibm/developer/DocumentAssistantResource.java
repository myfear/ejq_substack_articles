package com.ibm.developer;

import java.util.Map;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import com.ibm.developer.service.DocumentAssistant;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/assistant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentAssistantResource {


    @Inject
    DocumentAssistant documentAssistant;


    @POST
    @Path("/ask")
    @Counted(name = "questions_asked", description = "Number of questions asked")
    @Timed(name = "question_response_time", description = "Question response time", unit = MetricUnits.SECONDS)
    public Response askQuestion(Map<String, String> request) {
        String question = request.get("question");
        
        if (question == null || question.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", "question is required"))
                .build();
        }

        try {
            String answer = documentAssistant.answerQuestion(question);
            return Response.ok(Map.of(
                "question", question,
                "answer", answer
            )).build();
        } catch (Exception e) {
            Log.error("Error processing question: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Failed to process question: " + e.getMessage()))
                .build();
        }
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of(
            "status", "healthy",
            "service", "Document Assistant",
            "timestamp", System.currentTimeMillis()
        )).build();
    }
}
