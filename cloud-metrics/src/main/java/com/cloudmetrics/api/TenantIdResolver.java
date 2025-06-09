package com.cloudmetrics.api;

import io.quarkiverse.bucket4j.runtime.resolver.IdentityResolver;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class TenantIdResolver implements IdentityResolver {

    @Inject
    RoutingContext context;

    @Override
    public String getIdentityKey() {
        // Extract the tenant ID from the custom header
        String tenantId = context.request().getHeader("X-Tenant-ID");

        // If the header is not present, we can deny the request
        // or assign a default "anonymous" bucket. Here we deny.
        if (tenantId == null || tenantId.isBlank()) {
            // This will cause a 403 Forbidden because no key is resolved.
            return null;
        }
        return tenantId;
    }
}