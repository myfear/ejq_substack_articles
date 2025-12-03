package com.example.task;

import java.time.LocalDate;
import java.util.List;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class TaskService {

    public List<Task> findAll() {
        return Task.listAll(Sort.by("id").descending());
    }

    public long countOpen() {
        return Task.count("done = ?1", false);
    }

    public long countDone() {
        return Task.count("done = ?1", true);
    }

    @Transactional
    public Task create(String title, String description, LocalDate dueDate) {
        Task task = new Task();
        task.title = title;
        task.description = description;
        task.dueDate = dueDate;
        task.done = false;
        task.persist();
        return task;
    }

    @Transactional
    public Task toggleDone(Long id) {
        Task task = Task.findById(id);
        if (task == null) {
            return null;
        }
        task.done = !task.done;
        return task;
    }

    @Transactional
    public void delete(Long id) {
        Task.deleteById(id);
    }
}