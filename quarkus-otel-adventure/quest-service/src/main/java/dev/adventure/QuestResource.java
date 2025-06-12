package dev.adventure;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@Path("/quests")
public class QuestResource {

    private static final Logger LOG = Logger.getLogger(QuestResource.class);

    @Inject
    @RestClient
    ForgeClient forgeClient;

    @GET
    @Path("/start")
    public String startQuest() {
        LOG.info("Quest starting! We need a sword.");
        String result = forgeClient.craftSword();
        LOG.info("Quest update: " + result);
        return "Quest Started! Acquired: " + result;
    }
}
