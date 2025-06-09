package com.cloudmetrics.api;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class TenantFilter implements ContainerRequestFilter {

    @Inject
    TenantContext context;

    private static final Logger LOG = Logger.getLogger(TenantFilter.class);

    @Override
    public void filter(ContainerRequestContext req) {
        context.setTenantId(req.getHeaderString("X-Tenant-ID"));
        LOG.infof("X-Tenant-ID " + context.getTenantId());
    }
}