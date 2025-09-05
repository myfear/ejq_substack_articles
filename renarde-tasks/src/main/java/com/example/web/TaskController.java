package com.example.web;

import java.util.Date;
import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import com.example.model.Task;

import io.quarkiverse.renarde.Controller;
import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;

public class TaskController extends Controller {

    @CheckedTemplate
    static class Templates {
        public static native TemplateInstance index(List<Task> tasks);
    }

    public TemplateInstance index() {
        List<Task> tasks = Task.findAllOrderByDoneDate();
        return Templates.index(tasks);
    }

    @POST
    @Transactional
    public void add(@NotBlank @RestForm String task) {
        if (validationFailed()) {
            flash("error", "Description required");
            index();
        }
        Task newtask = new Task();
        newtask.description = task;
        newtask.persist();

        flash("message", "Task added");

        index();
    }

    @POST
    public void delete(@RestPath Long id) {
        // find the Task
        Task task = Task.findById(id);
        notFoundIfNull(task);
        // delete it
        task.delete();
        // show message
        flash("message", "Task deleted");
        // redirect to index page
        index();
    }

    @GET
    @Transactional
    public void toggle(@RestPath Long id) {
        Log.infof("Toggling task with id: %s", id);
        // find the Task
        Task task = Task.findById(id);
        notFoundIfNull(task);
        // switch the done state
        task.done = !task.done;
        if (task.done)
            task.doneDate = new Date();
        // send loving message
        flash("message", "Task updated");
        // redirect to index page
        index();
    }
}