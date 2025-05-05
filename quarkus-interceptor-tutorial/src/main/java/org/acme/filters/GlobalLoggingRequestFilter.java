package org.acme.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import java.io.IOException;

@Provider
public class GlobalLoggingRequestFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(GlobalLoggingRequestFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.infof("[GLOBAL] Received request: %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getAbsolutePath());

        requestContext.setProperty("request-start-time", System.nanoTime());
    }
}