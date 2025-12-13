package com.example.filter;

import java.util.UUID;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;

@Traced
public class TraceIdFilter {

    private static final Logger LOG = Logger.getLogger(TraceIdFilter.class);

    @ServerRequestFilter
    public void trace(ContainerRequestContext context) {
        String traceId = context.getHeaderString("X-Trace-ID");

        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            context.getHeaders().add("X-Trace-ID", traceId);
        }
        LOG.infof("TraceIdFilter LOG: %s", traceId);
    }

    @ServerResponseFilter
    public void addTraceIdToResponse(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String traceId = requestContext.getHeaderString("X-Trace-ID");
        if (traceId != null) {
            responseContext.getHeaders().add("X-Trace-ID", traceId);
        }
    }
}
