package com.example.interceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@RateLimited
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 1)
public class RateLimitInterceptor {

    private static final Logger LOG = Logger.getLogger(RateLimitInterceptor.class);

    @ConfigProperty(name = "app.rate-limit.max-requests", defaultValue = "30")
    int maxRequests;

    @ConfigProperty(name = "app.rate-limit.window-ms", defaultValue = "60000")
    long windowMs;

    private static class RateLimitStatus {
        final AtomicInteger count = new AtomicInteger(0);
        volatile long windowStart = System.currentTimeMillis();
    }

    private final Map<String, RateLimitStatus> clientRequestCounts = new ConcurrentHashMap<>();

    @Inject
    SecurityContext securityContext;

    @AroundInvoke
    public Object rateLimit(InvocationContext context) throws Exception {
        LOG.debugf("Rate limit check for method: %s", context.getMethod().getName());
        String clientId = getClientId();
        long now = System.currentTimeMillis();
        RateLimitStatus status = clientRequestCounts.computeIfAbsent(clientId, k -> new RateLimitStatus());

        if (now - status.windowStart > windowMs) {
            synchronized (status) {
                if (now - status.windowStart > windowMs) {
                    status.windowStart = now;
                    status.count.set(0);
                }
            }
        }

        if (status.count.incrementAndGet() > maxRequests) {
            LOG.warnf("Rate limit exceeded for client: %s", clientId);
            return Response.status(429, "Too Many Requests")
                           .entity("Rate limit exceeded. Please try again later.")
                           .build();
        }

        return context.proceed();
    }
    
    private String getClientId() {
        if (securityContext != null && securityContext.getUserPrincipal() != null) {
            return securityContext.getUserPrincipal().getName();
        }
        return "anonymous-client"; 
    }
}