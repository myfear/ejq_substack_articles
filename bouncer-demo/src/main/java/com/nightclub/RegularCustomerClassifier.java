package com.nightclub;

import io.quarkus.load.shedding.RequestClassifier;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RegularCustomerClassifier implements RequestClassifier<HttpServerRequest> {

    @Override
    public boolean appliesTo(Object request) {
        return true; // This classifier applies to all requests
    }

    @Override
    public int cohort(HttpServerRequest request) {
        // Check if they're a "regular" (via header)
        String customerType = request.getHeader("X-Customer-Type");

        if ("regular".equalsIgnoreCase(customerType)) {
            // Regular customers get a better cohort (lower number)
            return 10;
        } else if ("premium".equalsIgnoreCase(customerType)) {
            // Premium customers get even better treatment
            return 5;
        }

        // Random people get assigned based on IP (default behavior)
        return 64; // Middle of the road
    }
}