package org.acme.tracing;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/llm-traces")
public class LLMTraceResource {

    @Inject
    LLMCallTracker tracker;

    @Inject
    MermaidGraphGenerator mermaidGenerator;

    @GET
    @Path("/{conversationId}/trace")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrace(@PathParam("conversationId") String id) {
        return tracker.getTrace(id)
                .map(trace -> Response.ok(trace).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/{conversationId}/mermaid")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getMermaidDiagram(@PathParam("conversationId") String id) {
        return tracker.getTrace(id)
                .map(mermaidGenerator::generate)
                .map(mermaid -> Response.ok(mermaid).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}