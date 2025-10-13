package org.acme;

import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import java.net.InetAddress;
import java.util.UUID;

@Path("/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartResource {

    private static final Logger LOG = Logger.getLogger(CartResource.class);
    private final String instanceId = UUID.randomUUID().toString().substring(0, 8);
    private final String hostname;

    @Inject
    RedisDataSource redis;

    private ValueCommands<String, String> commands;

    public CartResource() {
        String tempHostname;
        try {
            tempHostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            tempHostname = "unknown";
        }
        this.hostname = tempHostname;
    }

    @PostConstruct
    void init() {
        commands = redis.value(String.class);
        LOG.info("CartResource instance " + instanceId + " started on " + hostname);
    }

    @POST
    public Response addItem(@HeaderParam("X-User-Id") String userId, String item) {
        if (userId == null || userId.isBlank()) {
            throw new WebApplicationException("Missing X-User-Id header", 400);
        }
        LOG.info("📦 Instance " + instanceId + " (" + hostname + ") handling POST request for user: " + userId + ", item: " + item);
        commands.set(userId + ":cart", item);
        return Response.ok("{\"status\":\"added\", \"instance\":\"" + instanceId + "\", \"hostname\":\"" + hostname + "\"}").build();
    }

    @GET
    public Response getItem(@HeaderParam("X-User-Id") String userId) {
        if (userId == null || userId.isBlank()) {
            throw new WebApplicationException("Missing X-User-Id header", 400);
        }
        LOG.info("🛒 Instance " + instanceId + " (" + hostname + ") handling GET request for user: " + userId);
        String value = commands.get(userId + ":cart");
        if (value == null) {
            return Response.ok("{\"cart\": [], \"instance\":\"" + instanceId + "\", \"hostname\":\"" + hostname + "\"}").build();
        }
        return Response.ok("{\"cart\": [\"" + value + "\"], \"instance\":\"" + instanceId + "\", \"hostname\":\"" + hostname + "\"}").build();
    }
}