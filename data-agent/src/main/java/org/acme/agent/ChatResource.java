package org.acme.agent;

import org.jboss.logging.Logger;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/chat")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
public class ChatResource {

    public static Logger LOG = Logger.getLogger(ChatResource.class);

    @Inject
    DataAgent agent;

    @POST
    public String chat(String userInput) {
        LOG.info("Chat request received: " + userInput);
        return agent.chat(userInput);
    }
}