package com.example.filter;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;

@Audited
@ApplicationScoped
public class RequestAuditFilter {

    private static final Logger LOG = Logger.getLogger(RequestAuditFilter.class);

    @Inject
    ObjectMapper objectMapper;

    @ServerRequestFilter
    public Uni<Void> captureAndInspect(ContainerRequestContext context, ResourceInfo resourceInfo) {
        // Check if the matched resource method has @Audited annotation
        if (resourceInfo != null) {
            Method resourceMethod = resourceInfo.getResourceMethod();
            if (resourceMethod != null) {
                boolean hasAudited = resourceMethod.isAnnotationPresent(Audited.class)
                                || resourceMethod.getDeclaringClass().isAnnotationPresent(Audited.class);

                if (!hasAudited) {
                    // Skip auditing if the resource method doesn't have @Audited
                    return Uni.createFrom().voidItem();
                }
            }
        }
        return Uni.createFrom().item(() -> {
            byte[] originalBytes = null;
            try {
                // Step 1: Access the raw request body stream
                var entityStream = context.getEntityStream();

                // Step 2: Read all bytes (this will be executed on a worker thread via Uni)
                originalBytes = entityStream.readAllBytes();

                // Step 3: Avoid noise for empty bodies (e.g. GET requests)
                if (originalBytes.length == 0) {
                    context.setEntityStream(new ByteArrayInputStream(originalBytes));
                    return null;
                }

                // Step 4: Convert to String and parse JSON
                String body = new String(originalBytes, StandardCharsets.UTF_8);
                
                try {
                    // Parse JSON to validate and pretty-print for audit log
                    JsonNode jsonNode = objectMapper.readTree(body);
                    String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
                    LOG.infof("AUDIT LOG:\n%s", prettyJson);
                    // In a real system, forward this to an async audit service
                } catch (Exception jsonException) {
                    // If it's not valid JSON, log as plain text
                    LOG.infof("AUDIT LOG (non-JSON): %s", body);
                }

                // Step 5: Reset the stream so downstream can read it
                context.setEntityStream(
                        new ByteArrayInputStream(originalBytes));

                return null;
            } catch (Exception e) {
                LOG.error("Failed to read request body", e);
                // If reading fails, return original body if available
                if (originalBytes != null) {
                    context.setEntityStream(new ByteArrayInputStream(originalBytes));
                }
                // Don't throw exception - let the request continue
                return null;
            }
        })
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .replaceWithVoid();
    }
}