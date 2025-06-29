package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

@Path("/progress")
public class ProgressResource {

    @Inject
    SseService sseService;

    @GET
    @Path("/{clientId}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void stream(@PathParam("clientId") String clientId,
            @Context SseEventSink sink,
            @Context Sse sse) {
        sseService.register(clientId, sink);
    }
}