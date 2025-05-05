package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.filters.Audited;
import org.jboss.logging.Logger;

@Path("/hello")
public class GreetingResource {

    private static final Logger LOG = Logger.getLogger(GreetingResource.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        LOG.info("Executing standard hello() method.");
        return "Hello from Quarkus";
    }

    @GET
    @Path("/audited")
    @Produces(MediaType.TEXT_PLAIN)
    @Audited
    public String helloAudited() {
        LOG.info("Executing AUDITED helloAudited() method.");
        return "Hello from the *audited* endpoint!";
    }
}
