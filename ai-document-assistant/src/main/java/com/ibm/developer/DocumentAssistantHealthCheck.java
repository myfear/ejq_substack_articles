package com.ibm.developer;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import com.ibm.developer.service.DocumentAssistant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Readiness
@ApplicationScoped
public class DocumentAssistantHealthCheck implements HealthCheck {

    @Inject
    DocumentAssistant documentAssistant;

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