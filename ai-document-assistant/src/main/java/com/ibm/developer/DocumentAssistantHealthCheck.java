package com.ibm.developer;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import com.ibm.developer.service.DocumentAssistant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * MicroProfile Health readiness check for the Document Assistant service.
 * 
 * <p>This health check verifies that the AI document assistant service is fully operational
 * and ready to handle requests. It performs an actual test query to ensure the entire
 * pipeline (embedding generation, vector search, and LLM response) is functioning correctly.</p>
 * 
 * <p>The health check is exposed at the {@code /q/health/ready} endpoint and is typically
 * used by container orchestration platforms (like Kubernetes) to determine if the service
 * is ready to receive traffic.</p>
 * 
 * <p><strong>Health Check Strategy:</strong> Issues a simple test question to the AI service
 * and verifies that a non-empty response is returned.</p>
 * 
 * @author IBM Developer
 * @version 1.0.0
 * @see DocumentAssistant
 */
@Readiness
@ApplicationScoped
public class DocumentAssistantHealthCheck implements HealthCheck {

    @Inject
    DocumentAssistant documentAssistant;

    /**
     * Executes the readiness health check by testing the document assistant service.
     * 
     * <p>This method sends a test question to the AI document assistant to verify that
     * the service is functioning correctly. A successful health check indicates that:</p>
     * <ul>
     *   <li>The embedding model is accessible</li>
     *   <li>The vector store is operational</li>
     *   <li>The LLM is available and responding</li>
     *   <li>The RAG pipeline is working end-to-end</li>
     * </ul>
     * 
     * @return a HealthCheckResponse indicating:
     *         <ul>
     *           <li>UP status if the assistant responds successfully</li>
     *           <li>DOWN status if an error occurs, with error details</li>
     *         </ul>
     */
    @Override
    public HealthCheckResponse call() {
        try {
            // Test the AI service with a simple question
            String response = documentAssistant.answerQuestion("Hello, are you working?");

            return HealthCheckResponse.named("document-assistant")
                    .status(response != null && !response.trim().isEmpty())
                    .withData("last-check", System.currentTimeMillis())
                    .build();

        } catch (Exception e) {
            return HealthCheckResponse.named("document-assistant")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}