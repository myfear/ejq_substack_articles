package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/tasks")
public class TaskResource {

    @Inject
    AgentSpawner agentSpawner;

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public String createTask(String goal) {
        if (goal == null || goal.isBlank()) {
            return "Please provide a goal in the request body.";
        }
        return agentSpawner.spawnAndExecute(goal);
    }
}