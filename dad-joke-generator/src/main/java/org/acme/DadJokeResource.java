package org.acme;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class DadJokeResource {
    @Inject
    DadJokeService jokeService;
    @Inject
    Template dadjoke;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return dadjoke.data("joke", jokeService.getDadJoke());
    }
}