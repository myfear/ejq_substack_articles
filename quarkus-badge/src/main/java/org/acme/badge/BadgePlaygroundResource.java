package org.acme.badge;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/playground")
@ApplicationScoped
public class BadgePlaygroundResource {

    @Inject
    Template badge_playground; // matches badge_playground.html

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance playground(
            @QueryParam("label") @DefaultValue("Built with") String label,
            @QueryParam("value") @DefaultValue("Quarkus") String value,
            @QueryParam("theme") @DefaultValue("default") String theme) {
        String badgeUrl = "/badge/dynamic.svg?label=" 
                + URLEncoder.encode(label, StandardCharsets.UTF_8)
                + "&value=" + URLEncoder.encode(value, StandardCharsets.UTF_8)
                + "&theme=" + URLEncoder.encode(theme, StandardCharsets.UTF_8);

        return badge_playground
                .data("label", label)
                .data("value", value)
                .data("theme", theme)
                .data("badgeUrl", badgeUrl);
    }
}