package com.example;

import java.time.Instant;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.http.HttpServerRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuditFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger AUDIT_LOGGER = Logger.getLogger("AuditLogger");
    private static final String AUDIT_RECORD_PROPERTY = "auditRecord";

    @Inject
    JsonWebToken jwt;

    @Inject
    HttpServerRequest request;

    @Inject
    ObjectMapper objectMapper;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        if (requestContext.getUriInfo().getPath().contains("transactions")) {
            AuditRecord record = new AuditRecord();
            record.timestamp = Instant.now();
            record.httpMethod = requestContext.getMethod();
            record.resourcePath = requestContext.getUriInfo().getPath();
            record.clientIp = request.remoteAddress().toString();
            record.principal = jwt.getRawToken() != null ? jwt.getName() : "anonymous";
            requestContext.setProperty(AUDIT_RECORD_PROPERTY, record);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        AuditRecord record = (AuditRecord) requestContext.getProperty(AUDIT_RECORD_PROPERTY);
        if (record != null) {
            record.httpStatus = responseContext.getStatus();
            try {
                AUDIT_LOGGER.info(objectMapper.writeValueAsString(record));
            } catch (Exception e) {
                // Fail silently in audit logger
            }
        }
    }
}