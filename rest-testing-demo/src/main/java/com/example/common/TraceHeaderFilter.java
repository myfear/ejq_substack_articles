package com.example.common;

import java.util.UUID;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
@Priority(Priorities.HEADER_DECORATOR)
public class TraceHeaderFilter implements ContainerResponseFilter {
    @Override
    public void filter(jakarta.ws.rs.container.ContainerRequestContext req, ContainerResponseContext res) {
        MultivaluedMap<String, Object> headers = res.getHeaders();
        headers.putSingle("X-Trace-Id", UUID.randomUUID().toString());
    }
}