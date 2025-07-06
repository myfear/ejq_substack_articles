package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/inspect")
public class InspectorResource {

    @Inject
    JvmInspector ai;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String inspect(@QueryParam("query") String query) {
        return ai.chat(query);
    }
}