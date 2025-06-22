package com.secretagent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RateLimitingInterceptor implements ContainerRequestFilter {
    
    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String apiKey = requestContext.getHeaderString("X-API-Key");
        if (apiKey != null) {
            AtomicInteger count = requestCounts.computeIfAbsent(apiKey, k -> new AtomicInteger(0));
            
            if (count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
                requestContext.abortWith(
                    Response.status(Response.Status.TOO_MANY_REQUESTS)
                        .entity(new ApiKeyResource.ErrorResponse("Rate limit exceeded, Agent!"))
                        .build()
                );
            }
        }
    }
}