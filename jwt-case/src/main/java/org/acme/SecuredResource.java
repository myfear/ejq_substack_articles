package org.acme;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("/secured")
@RequestScoped
public class SecuredResource {

    @Inject
    JsonWebToken jwt;
    @Context
    SecurityContext ctx;

private static final Logger LOG = Logger.getLogger(SecuredResource.class);

    @GET
    @Path("/hello")
    @RolesAllowed({ "user", "admin" })
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@Context SecurityContext ctx) {
        LOG.info(getResponseString(ctx));
        String name = jwt.getName();
        boolean isAdmin = jwt.getGroups().contains("admin");
        return "Hello " + name + "! Roles: " + jwt.getGroups() + (isAdmin ? " You're an admin." : "");
    }

    @GET
    @Path("/admin")
    @RolesAllowed("admin")
    @Produces(MediaType.TEXT_PLAIN)
    public String admin() {
        return "Welcome Admin: " + jwt.getName() + ". Your JWT ID: " + jwt.getTokenID();
    }

    @GET
    @Path("/public")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    public String publicInfo() {
        if (ctx.getUserPrincipal() != null)
            return "Hello " + ctx.getUserPrincipal().getName() + "! (token detected)";
        return "This is a public endpoint, Guest.";
    }

    private String getResponseString(SecurityContext ctx) {
        String name;
        if (ctx.getUserPrincipal() == null) {
            name = "anonymous";
        } else if (!ctx.getUserPrincipal().getName().equals(jwt.getName())) {
            throw new InternalServerErrorException("Principal and JsonWebToken names do not match");
        } else {
            name = ctx.getUserPrincipal().getName();
        }
        return String.format("hello %s,"
                + " isHttps: %s,"
                + " authScheme: %s,"
                + " hasJWT: %s",
                name, ctx.isSecure(), ctx.getAuthenticationScheme(), hasJwt());
    }

    private boolean hasJwt() {
        return jwt.getClaimNames() != null;
    }

}
