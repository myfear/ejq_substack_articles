package org.example;

import java.nio.file.Paths;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class HllResource {
    @Inject
    CardinalityService service;
    @Inject
    DataIngestor ingestor;

    @POST
    @Path("/ingest")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ingest(Map<String, String> input) {
        try {
            long count = ingestor.processGhArchiveFile(Paths.get(input.get("path")));
            return Response.ok(Map.of("status", "ok", "lines", count)).build();
        } catch (Exception e) {
            return Response.serverError().entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/stats/unique-users/today")
    public Response users() {
        return Response.ok(Map.of("estimate", service.getDailyUserEstimate())).build();
    }

    @GET
    @Path("/stats/unique-repos/weekly")
    public Response repos() {
        return Response.ok(Map.of("estimate", service.getWeeklyRepoEstimate())).build();
    }

    @GET
    @Path("/stats/memory-usage")
    public Response mem() {
        return Response.ok(Map.of("bytes", service.getMemoryUsageBytes())).build();
    }
}
