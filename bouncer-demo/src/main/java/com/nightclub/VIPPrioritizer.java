package com.nightclub;

import io.quarkus.load.shedding.RequestPrioritizer;
import io.quarkus.load.shedding.RequestPriority;
import io.vertx.core.http.HttpServerRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class VIPPrioritizer implements RequestPrioritizer<HttpServerRequest> {

    private static final Logger LOG = Logger.getLogger(VIPPrioritizer.class);

    @Override
    public boolean appliesTo(Object request) {
        return true; // This prioritizer applies to all requests
    }

    @Override
    public RequestPriority priority(HttpServerRequest request) {
        String path = request.path();
        LOG.debugf("VIPPrioritizer called for path: %s", path);

        // VIP endpoints get CRITICAL priority
        if (path.contains("/vip-")) {
            return RequestPriority.CRITICAL;
        }

        // Regular entry is NORMAL
        if (path.contains("/enter")) {
            return RequestPriority.NORMAL;
        }

        // Bathroom can wait (sorry!)
        if (path.contains("/bathroom")) {
            return RequestPriority.BACKGROUND;
        }

        return RequestPriority.NORMAL;
    }
}