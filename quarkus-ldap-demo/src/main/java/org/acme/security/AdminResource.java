package org.acme.security;

import org.jboss.logging.Logger;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/admin")
public class AdminResource {

    private static final Logger LOG = Logger.getLogger(AdminResource.class);

    @Context
    SecurityContext securityContext;

    @Inject
    SecurityIdentity securityIdentity;

    @GET
    @RolesAllowed("adminRole")
    @Produces(MediaType.TEXT_PLAIN)
    public String adminResource() {
        if (securityContext.getUserPrincipal() != null) {
            LOG.infof("Admin endpoint accessed by user: %s", securityContext.getUserPrincipal().getName());
            LOG.infof("User principal class: %s", securityContext.getUserPrincipal().getClass().getName());
            LOG.infof("User has adminRole: %s", securityContext.isUserInRole("adminRole"));
            LOG.infof("Authentication scheme: %s", securityContext.getAuthenticationScheme());
            LOG.infof("Is secure: %s", securityContext.isSecure());

            // Access roles and groups from SecurityIdentity
            LOG.infof("User roles: %s", securityIdentity.getRoles());
            LOG.infof("User attributes: %s", securityIdentity.getAttributes());

            // Check specific roles
            LOG.infof("Has adminRole: %s", securityIdentity.hasRole("adminRole"));
            LOG.infof("Has standardRole: %s", securityIdentity.hasRole("standardRole"));

            // Access LDAP-specific attributes if available
            securityIdentity.getAttributes().forEach((key, value) -> {
                LOG.infof("Attribute %s: %s", key, value);
            });

        } else {
            LOG.warn("Admin endpoint accessed without authenticated user");
        }

        return "admin";
    }
}