package org.acme;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class TodoService {

    private static final Logger log = Logger.getLogger(TodoService.class);
    private final List<String> tasks = new CopyOnWriteArrayList<>();

    @Tool("Adds a task to the Todo list based on the email content.")
    public String addTask(String task) {
        log.infof("AI TOOL: Adding task -> %s", task);
        tasks.add(task);
        return "Task added successfully: " + task;
    }

    public List<String> getTasks() {
        return List.copyOf(tasks);
    }
}