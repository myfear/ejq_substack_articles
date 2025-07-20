package com.example;

import java.util.List;

import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/page/todos")
@ApplicationScoped
public class PageResource {

    @Inject
    Template todos;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get() {
        List<Todo> todoList = Todo.listAll();
        return todos.data("todos", todoList).render();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String add(@FormParam("title") String title) {
        Todo newTodo = new Todo();
        newTodo.title = title;
        newTodo.completed = false;
        newTodo.persist();
        return get(); // Re-render updated page
    }
}