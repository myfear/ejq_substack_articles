package com.example.version;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

/**
 * Reads X-API-Version and stores it in VersionContext.
 */
@Provider
@ApplicationScoped
public class ApiVersionFilter implements ContainerRequestFilter {

    @ConfigProperty(name = "api.default-version", defaultValue = "2024-09-01")
    String defaultVersion;

    @Inject
    VersionContext ctx;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String v = requestContext.getHeaderString("X-API-Version");
        ctx.set(v != null && !v.isBlank() ? v : defaultVersion);
    }
}