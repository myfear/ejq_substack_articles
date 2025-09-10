package org.acme.todo;

import java.net.URI;
import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/todos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TodoResource {

    @GET
    public List<Todo> list() {
        return Todo.listAll();
    }

    @GET
    @Path("{id}")
    public Todo get(@PathParam("id") Long id) {
        Todo todo = Todo.findById(id);
        if (todo == null)
            throw new NotFoundException();
        return todo;
    }

    @POST
    @Transactional
    public Response create(@Valid Todo todo) {
        todo.id = null;
        todo.persist();
        return Response.created(URI.create("/api/todos/" + todo.id)).entity(todo).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Todo update(@PathParam("id") Long id, @Valid Todo changes) {
        Todo todo = Todo.findById(id);
        if (todo == null)
            throw new NotFoundException();
        todo.title = changes.title;
        todo.done = changes.done;
        return todo;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public void delete(@PathParam("id") Long id) {
        if (!Todo.deleteById(id))
            throw new NotFoundException();
    }
}