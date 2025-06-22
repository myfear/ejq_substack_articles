package com.secretagent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class ApiKeyHealthCheck implements HealthCheck {

    @Inject
    ApiKeyService apiKeyService;

    @Override
    public HealthCheckResponse call() {
        try {
            int activeKeys = apiKeyService.getAllActiveKeys().size();
            return HealthCheckResponse.named("API Key Service")
                    .up()
                    .withData("activeKeys", activeKeys)
                    .build();
        } catch (Exception e) {
            return HealthCheckResponse.named("API Key Service")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}