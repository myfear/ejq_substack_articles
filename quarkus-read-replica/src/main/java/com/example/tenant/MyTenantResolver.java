package com.example.tenant;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
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
        if (resource != null) {
            if (resource.getResourceMethod().isAnnotationPresent(ReadWrite.class)
                    || resource.getResourceClass().isAnnotationPresent(ReadWrite.class)) {
                return "read-write";
            }
        }
        return getDefaultTenantId();
    }
}