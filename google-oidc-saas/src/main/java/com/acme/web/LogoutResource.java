package com.acme.web;

import io.quarkus.oidc.OidcSession;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/logout")
public class LogoutResource {
    
    @Inject
    OidcSession oidcSession;

    @GET
    public Response logout() {
        // Use OidcSession for local logout - this will clear the local session
        // and redirect to the home page
        oidcSession.logout().await().indefinitely();
        return Response.seeOther(java.net.URI.create("/")).build();
    }
}