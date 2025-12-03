package com.ibm.developer.example;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/projects")
public class ProjectResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Project> list() {
        return List.of(
                new Project(1, "Carbon Starter", "Frontend Team", "Active"),
                new Project(2, "Quarkus Migration", "Platform Team", "Planning"),
                new Project(3, "AI Dashboard", "Data Team", "On hold"));
    }
}