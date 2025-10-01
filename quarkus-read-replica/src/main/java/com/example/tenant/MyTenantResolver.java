package com.example.tenant;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceInfo;

@PersistenceUnitExtension
@RequestScoped
public class MyTenantResolver implements TenantResolver {

    @Inject
    ResourceInfo resource;

    @Override
    public String getDefaultTenantId() {
        return "read-only";
    }

    @Override
    public String resolveTenantId() {
        String tenant = getDefaultTenantId();
        if (resource != null && (resource.getResourceMethod().isAnnotationPresent(ReadWrite.class)
                || resource.getResourceClass().isAnnotationPresent(ReadWrite.class))) {
            tenant = "read-write";
        }
        Log.infof("Resolved tenant: %s for %s", tenant,
                resource != null ? resource.getResourceMethod().getName() : "unknown");
        return tenant;
    }
}