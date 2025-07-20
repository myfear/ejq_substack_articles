package com.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Liveness
@ApplicationScoped
public class ExternalSitesHealthCheck implements HealthCheck {

    @Inject
    SitesConfig sitesConfig;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder responseBuilder = HealthCheckResponse.named("External Websites");

        boolean allSitesUp = true;

        for (String siteUrl : sitesConfig.sites()) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(siteUrl))
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                
                if (response.statusCode() >= 400) {
                    responseBuilder.withData(siteUrl, "DOWN");
                    allSitesUp = false;
                } else {
                    responseBuilder.withData(siteUrl, "UP");
                }
            } catch (Exception e) {
                responseBuilder.withData(siteUrl, "DOWN - " + e.getMessage());
                allSitesUp = false;
            }
        }

        return responseBuilder.status(allSitesUp).build();
    }
}
