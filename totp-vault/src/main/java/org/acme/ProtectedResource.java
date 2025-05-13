package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/protected")
public class ProtectedResource {

    @Inject
    VaultService vaultService;

    @GET
    public Response access(@HeaderParam("X-User") String username,
                           @HeaderParam("X-TOTP-Code") String code) {
        if (vaultService.validateCode(username, code)) {
            return Response.ok("Access granted").build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid TOTP code").build();
        }
    }
}