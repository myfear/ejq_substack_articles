package org.acme.todo;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

@RequestScoped
@PersistenceUnitExtension
public class MyTenantResolver implements TenantResolver {

    @Inject
    SecurityIdentity securityIdentity;

    @Override
    public String getDefaultTenantId() {
        return "UNKNOWN_TENANT";
    }

    @Override
    public String resolveTenantId() {
        if (securityIdentity != null && securityIdentity.isAnonymous() == false) {
            return securityIdentity.getPrincipal().getName();
        }
        return getDefaultTenantId();
    }
}