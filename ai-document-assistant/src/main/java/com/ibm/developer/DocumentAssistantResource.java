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

/**
 * REST resource providing endpoints for the AI Document Assistant service.
 * 
 * <p>This resource exposes HTTP endpoints for asking questions about ingested documents
 * and checking service health. It leverages LangChain4j for AI-powered document analysis
 * and includes metrics tracking for monitoring purposes.</p>
 * 
 * @author IBM Developer
 * @version 1.0.0
 */
@Path("/api/assistant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentAssistantResource {


    @Inject
    DocumentAssistant documentAssistant;


    /**
     * Processes a question about the ingested documents using AI.
     * 
     * <p>This endpoint accepts a question in JSON format and returns an AI-generated
     * answer based on the context of previously ingested documents. The response includes
     * both the original question and the generated answer.</p>
     * 
     * <p>Metrics are automatically tracked for the number of questions asked and
     * response time for monitoring and observability purposes.</p>
     * 
     * @param request a map containing the question with key "question"
     * @return a Response object containing:
     *         <ul>
     *           <li>200 OK with question and answer on success</li>
     *           <li>400 BAD REQUEST if question is missing or empty</li>
     *           <li>500 INTERNAL SERVER ERROR if processing fails</li>
     *         </ul>
     * 
     * @see DocumentAssistant#answerQuestion(String)
     */
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

    /**
     * Provides a simple health check endpoint for the Document Assistant service.
     * 
     * <p>This endpoint returns basic service status information including the service name
     * and current timestamp. For more comprehensive health checks, see the MicroProfile
     * Health checks implementation.</p>
     * 
     * @return a Response with status 200 OK containing service status information
     * 
     * @see DocumentAssistantHealthCheck
     */
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
