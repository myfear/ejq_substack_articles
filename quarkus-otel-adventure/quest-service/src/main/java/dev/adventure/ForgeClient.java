package dev.adventure;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/forge")
@RegisterRestClient(configKey = "forge-api")
public interface ForgeClient {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    String craftSword();
}
