package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/poem")
public class PoemResource {

    @Inject
    PoemService poemService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String writePoem(@QueryParam("topic") String topic, @QueryParam("style") String style) {
        if (topic == null || topic.isBlank()) {
            topic = "the wonders of programming";
        }
        if (style == null || style.isBlank()) {
            style = "a haiku";
        }
        return poemService.writePoem(topic, style);
    }
}