package org.acme.age;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class PageResource {
    @Inject
    Template age;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance page() {
        return age.instance();
    }
}