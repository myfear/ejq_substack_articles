package dev.mainthread.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
@ApplicationScoped
public class HttpRequestLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = Logger.getLogger(HttpRequestLoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) {
        LOG.infof(">>> HTTP Request: %s %s from %s", 
                requestContext.getMethod(),
                requestContext.getUriInfo().getPath(),
                requestContext.getHeaderString("X-Forwarded-For"));
        LOG.infof(">>> Headers: %s", requestContext.getHeaders());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        LOG.infof("<<< HTTP Response: %s %s -> %d %s", 
                requestContext.getMethod(),
                requestContext.getUriInfo().getPath(),
                responseContext.getStatus(),
                responseContext.getStatusInfo().getReasonPhrase());
        if (responseContext.getEntity() != null) {
            LOG.infof("<<< Response body: %s", responseContext.getEntity());
        }
    }
}

