package com.example.filter;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ResourceInfo;

@Sanitized
@ApplicationScoped
public class SanitizationFilter {

        private static final Logger LOG = Logger.getLogger(SanitizationFilter.class);
        private static final Set<String> SENSITIVE_FIELDS = Set.of("password", "secret", "token", "apiKey", "apikey",
                        "accessToken", "refreshToken", "ssn", "creditCard", "creditcard");

        @Inject
        ObjectMapper objectMapper;

        @ServerRequestFilter
        public Uni<Void> sanitize(ContainerRequestContext context, ResourceInfo resourceInfo) {
                // Check if the matched resource method has @Sanitized annotation
                if (resourceInfo != null) {
                        Method resourceMethod = resourceInfo.getResourceMethod();
                        if (resourceMethod != null) {
                                boolean hasSanitized = resourceMethod.isAnnotationPresent(Sanitized.class)
                                                || resourceMethod.getDeclaringClass()
                                                                .isAnnotationPresent(Sanitized.class);

                                if (!hasSanitized) {
                                        // Skip sanitization if the resource method doesn't have @Sanitized
                                        return Uni.createFrom().voidItem();
                                }
                        }
                }

                return Uni.createFrom().item(() -> {
                        byte[] originalBytes = null;
                        try {
                                LOG.infof("SanitizationFilter LOG");
                                originalBytes = context.getEntityStream().readAllBytes();

                                if (originalBytes.length == 0) {
                                        // Empty body, nothing to sanitize
                                        context.setEntityStream(new ByteArrayInputStream(originalBytes));
                                        return null;
                                }

                                String body = new String(originalBytes, StandardCharsets.UTF_8);

                                // Parse JSON and sanitize sensitive fields
                                JsonNode jsonNode = objectMapper.readTree(body);
                                sanitizeJsonNode(jsonNode);

                                // Convert back to JSON string
                                String sanitized = objectMapper.writeValueAsString(jsonNode);

                                context.setEntityStream(
                                                new ByteArrayInputStream(
                                                                sanitized.getBytes(StandardCharsets.UTF_8)));
                                return null;
                        } catch (Exception e) {
                                LOG.error("Failed to sanitize request body", e);
                                // If JSON parsing fails, return original body
                                if (originalBytes != null) {
                                        context.setEntityStream(new ByteArrayInputStream(originalBytes));
                                }
                                // Don't throw exception - let the request continue with original body
                                return null;
                        }
                })
                                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                                .replaceWithVoid();
        }

        private void sanitizeJsonNode(JsonNode node) {
                if (node.isObject()) {
                        ObjectNode objectNode = (ObjectNode) node;
                        objectNode.fieldNames().forEachRemaining(fieldName -> {
                                JsonNode fieldValue = objectNode.get(fieldName);

                                // Check if this is a sensitive field
                                if (SENSITIVE_FIELDS.contains(fieldName.toLowerCase())) {
                                        objectNode.put(fieldName, "********");
                                } else if (fieldValue.isObject() || fieldValue.isArray()) {
                                        // Recursively sanitize nested objects and arrays
                                        sanitizeJsonNode(fieldValue);
                                }
                        });
                } else if (node.isArray()) {
                        // Recursively sanitize each element in the array
                        for (JsonNode arrayElement : node) {
                                sanitizeJsonNode(arrayElement);
                        }
                }
        }
}
