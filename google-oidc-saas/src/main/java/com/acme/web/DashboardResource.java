package com.acme.web;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.acme.tenant.AppUser;
import com.acme.tenant.Tenant;

import io.quarkus.oidc.IdToken;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/dashboard")
@RequestScoped
public class DashboardResource {

    @Inject
    Template dashboard;

    @Inject
    @IdToken
    JsonWebToken idToken;

    private static String domainOf(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(at + 1).toLowerCase() : "unknown";
    }

    @GET
    @Transactional
    public TemplateInstance view() {
        String userEmail = idToken.getClaim("email");
        String userName = idToken.getClaim("preferred_username");
        String userPicture = idToken.getClaim("picture");

        Tenant tenant = Tenant.getOrCreate(domainOf(userEmail));
        AppUser.upsert(userEmail, userName, userPicture, tenant);

        return dashboard.instance()
                .data("userEmail", userEmail)
                .data("userName", userName)
                .data("userPicture", userPicture)
                .data("tenantDomain", tenant.domain);
    }
}