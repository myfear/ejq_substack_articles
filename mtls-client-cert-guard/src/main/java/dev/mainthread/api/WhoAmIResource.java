package dev.mainthread.api;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

@Path("/whoami")
public class WhoAmIResource {

    private static final Logger LOG = Logger.getLogger(WhoAmIResource.class);

    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("mtls-client")
    public WhoAmIResponse whoAmI() {
        LOG.infof("whoAmI() called, principal: %s", identity.getPrincipal().getName());
        String fingerprint = identity.getAttribute("clientCertFingerprint");
        WhoAmIResponse response = new WhoAmIResponse(identity.getPrincipal().getName(), fingerprint);
        LOG.infof("Returning response: %s", response);
        return response;
    }

    public record WhoAmIResponse(String principal, String fingerprint) {
    }
}