package com.example.project;

import java.util.List;

import com.example.auth.Action;
import com.example.auth.CheckAccess;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectResource {

    @GET
    @CheckAccess(resourceType = "Project", action = Action.READ)
    public Response list() {
        List<Project> projects = Project.listAll();
        return Response.ok(projects).build();
    }

    @POST
    @Transactional
    @CheckAccess(resourceType = "Project", action = Action.CREATE)
    public Response create(Project project) {
        project.persist();
        return Response.status(Response.Status.CREATED).entity(project).build();
    }
}