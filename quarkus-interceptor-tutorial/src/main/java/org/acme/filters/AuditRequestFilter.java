package org.acme.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;

@Provider
@Audited
public class AuditRequestFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(AuditRequestFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.infof("[AUDIT] Performing detailed audit check for: %s %s",
                requestContext.getMethod(),
                requestContext.getUriInfo().getPath());
    }
}
