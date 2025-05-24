package org.acme;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.acme.agent.DataAgent;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;

@Path("/")
public class ChatPageResource {

public static Logger LOG = Logger.getLogger(ChatPageResource.class);

    @Inject
    DataAgent agent;

    @Inject
    Template chat;

    @POST
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getPage(@RestForm String userInput) {
        String response = agent.chat(userInput);
        LOG.info("Chat request received: " + userInput);
        return chat.data("userInput", userInput, "response", response);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        return chat.render();
    }

}
