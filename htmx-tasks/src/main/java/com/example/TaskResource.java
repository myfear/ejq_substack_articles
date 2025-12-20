package com.example;

import java.util.List;

import com.example.model.Task;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/tasks")
public class TaskResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance tasks(List<Task> tasks);

        public static native TemplateInstance taskItem(Task task);

        public static native TemplateInstance taskEditForm(Task task);

        public static native TemplateInstance taskList(List<Task> tasks);

        public static native TemplateInstance taskFormErrors(List<String> errors);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance list() {
        List<Task> tasks = Task.listAll();
        return Templates.tasks(tasks);
    }

    @POST
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public Response create(@FormParam("title") String title) {
        // Validate
        if (title == null || title.trim().length() < 3) {
            return Response.status(422)
                    .entity(Templates.taskFormErrors(
                            List.of("Title must be at least 3 characters")))
                    .build();
        }

        if (title.length() > 100) {
            return Response.status(422)
                    .entity(Templates.taskFormErrors(
                            List.of("Title must be less than 100 characters")))
                    .build();
        }

        // Create task
        Task task = new Task();
        task.title = title.trim();
        task.completed = false;
        task.persist();

        return Response.ok(Templates.taskItem(task)).build();
    }

    @GET
    @Path("/{id}/view")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance view(@PathParam("id") Long id) {
        Task task = Task.findById(id);
        if (task == null) {
            throw new NotFoundException();
        }
        return Templates.taskItem(task);
    }

    @GET
    @Path("/{id}/edit")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance editForm(@PathParam("id") Long id) {
        Task task = Task.findById(id);
        if (task == null) {
            throw new NotFoundException();
        }
        return Templates.taskEditForm(task);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Produces(MediaType.TEXT_HTML)
    public Response update(@PathParam("id") Long id,
            @FormParam("title") String title) {
        Task task = Task.findById(id);
        if (task == null) {
            throw new NotFoundException();
        }

        // Validate
        if (title == null || title.trim().length() < 3) {
            return Response.status(422)
                    .entity(Templates.taskFormErrors(
                            List.of("Title must be at least 3 characters")))
                    .build();
        }

        if (title.length() > 100) {
            return Response.status(422)
                    .entity(Templates.taskFormErrors(
                            List.of("Title must be less than 100 characters")))
                    .build();
        }

        task.title = title.trim();
        task.persist();

        return Response.ok(Templates.taskItem(task)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        boolean deleted = Task.deleteById(id);
        if (!deleted) {
            throw new NotFoundException();
        }
        return Response.ok().build();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance search(@QueryParam("q") String query) {
        List<Task> results;

        if (query == null || query.isBlank()) {
            results = Task.listAll();
        } else {
            results = Task.list("lower(title) like lower(?1)",
                    "%" + query.trim() + "%");
        }

        return Templates.taskList(results);
    }
}