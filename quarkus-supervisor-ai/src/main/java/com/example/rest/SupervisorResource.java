package com.example.rest;

import java.util.List;

import com.example.messaging.AgentTask;
import com.example.supervisor.SupervisorService;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/supervisor")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SupervisorResource {

    @Inject
    SupervisorService supervisorService;

    // A simple request body for the /analyze endpoint
    public record AnalysisRequest(String content) {
    }

    @POST
    @Path("/analyze")
    public Uni<Response> analyzeContent(AnalysisRequest request) {
        var workflowRequest = new SupervisorService.WorkflowRequest(
                List.of(
                        new AgentTask("RESEARCH", request.content(), 1),
                        new AgentTask("ANALYSIS", request.content(), 1),
                        new AgentTask("SUMMARY", request.content(), 1)));

        return supervisorService.executeWorkflow(workflowRequest)
                .map(results -> Response.ok(results).build());
    }

    @POST
    @Path("/custom-workflow")
    public Uni<Response> customWorkflow(SupervisorService.WorkflowRequest request) {
        return supervisorService.executeWorkflow(request)
                .map(results -> Response.ok(results).build());
    }
}