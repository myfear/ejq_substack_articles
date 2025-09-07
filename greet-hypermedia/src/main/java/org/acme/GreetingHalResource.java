package org.acme;

import org.jboss.resteasy.reactive.common.util.RestMediaType;

import io.quarkus.resteasy.reactive.links.InjectRestLinks;
import io.quarkus.resteasy.reactive.links.RestLink;
import io.quarkus.resteasy.reactive.links.RestLinkType;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/greeting-hal")
@Produces(MediaType.APPLICATION_JSON)
public class GreetingHalResource {

    @GET
    @Produces({ MediaType.APPLICATION_JSON, RestMediaType.APPLICATION_HAL_JSON })
    @Path("/{name}")
    @RestLink(rel = "self")
    @InjectRestLinks(RestLinkType.INSTANCE)
    public Greeting greet(@PathParam("name") String name) {
        return new Greeting(name, "Hello, " + name + "!");
    }

}
