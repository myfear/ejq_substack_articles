package com.example;

import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/assign-chores")
public class ChoreResource {

    @Inject
    ChoreAssignmentService assignmentService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createChoreAssignment(ChoreAssignmentRequest request) {
        Map<String, String> stableAssignment = assignmentService.findStableAssignment(request);
        return Response.ok(stableAssignment).build();
    }
}