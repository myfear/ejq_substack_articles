package com.example;

import org.jboss.logging.Logger;

import com.example.interceptor.RateLimited;
import com.example.service.AviationAiService;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/flights")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FlightTrackerResource {
    
    private static final Logger LOG = Logger.getLogger(FlightTrackerResource.class);
    
    @Inject
    AviationAiService aviationAiService;
    
    @POST
    @Path("/query")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @RateLimited
    public Response processQuery(@NotBlank String query) {
        try {
            LOG.infof("Processing query: %s", query);
            String response = aviationAiService.processQuery(query);
            return Response.ok(response).build();
        } catch (Exception e) {
            LOG.errorf(e, "Error processing query: %s", query);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing your query: " + e.getMessage())
                    .build();
        }
    }
    
    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    public Response health() {
        return Response.ok("Flight Tracker AI (Ollama) is running!").build();
    }
}