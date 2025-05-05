package org.acme.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.io.IOException;
import java.util.UUID;

@Provider
public class GlobalHeaderResponseFilter implements ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(GlobalHeaderResponseFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        responseContext.getHeaders().add(
                "X-Global-Trace-ID",
                UUID.randomUUID().toString().substring(0, 8));

        Object startTimeObj = requestContext.getProperty("request-start-time");
        if (startTimeObj instanceof Long) {
            long durationNanos = System.nanoTime() - (Long) startTimeObj;
            LOG.infof("[GLOBAL] Request processed in %.2f ms", durationNanos / 1_000_000.0);
        }
        LOG.info("[GLOBAL] Response filter finished.");
    }
}