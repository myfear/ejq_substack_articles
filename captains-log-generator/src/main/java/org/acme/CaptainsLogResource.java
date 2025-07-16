package org.acme;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class CaptainsLogResource {

    @Inject
    CaptainsLogService logService;

    @Inject
    Template log;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        return log.data("entry", logService.generateLog()).render();
    }
}