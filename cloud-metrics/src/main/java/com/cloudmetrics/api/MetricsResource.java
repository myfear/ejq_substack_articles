package com.cloudmetrics.api;

import io.quarkiverse.bucket4j.runtime.RateLimited;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/v1")
@Produces(MediaType.TEXT_PLAIN)
public class MetricsResource {

    @Inject
    TenantService tenantService;

    @Inject
    TenantContext tenantContext;

    @GET
    @Path("/metrics/cpu")
    @RateLimited(bucket = "cpu-metrics-limit")
    public String getCpuMetrics() {
        return "CPU usage: 42%";
    }

    @GET
    @Path("/metrics/memory")
    @RateLimited(bucket = "memory-metrics-limit", identityResolver = TenantIdResolver.class)
    public String getMemoryMetrics() {
        return "Memory usage: 58%";
    }

    @GET
    @Path("/reports/generate")
    @DynamicRateLimited
    public String generateReport() {
        return "Report generated for " + tenantContext.getTenantId();
    }
}