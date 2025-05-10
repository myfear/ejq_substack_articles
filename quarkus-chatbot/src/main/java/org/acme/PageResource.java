package org.acme;

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
    Template chat;

    @Inject
    ChatAgent agent2;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance index() {
        return chat.instance();
    }

    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return agent2.chat("Who are you?");
    }
}