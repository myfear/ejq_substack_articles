package com.example;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TenantFilter implements ContainerRequestFilter {

    @Inject
    TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String host = requestContext.getHeaderString("Host");
        String tenantId = extractTenantFromHost(host);
        tenantContext.setTenantId(tenantId);
        System.out.println("🏢 Tenant detected: " + tenantId + " from host: " + host);
    }

    private String extractTenantFromHost(String host) {
        if (host == null) {
            return "default";
        }

        // Extract tenant from patterns like:
        // acme.127-0-0-1.nip.io:8080 → acme
        // techstart.192.168.1.100.nip.io → techstart
        String[] parts = host.split("\\.");

        if (parts.length >= 2 && host.contains("nip.io")) {
            return parts[0]; // First subdomain is the tenant
        }
        return "default";
    }
}