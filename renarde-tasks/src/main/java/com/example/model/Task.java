package com.example.model;

import java.util.Date;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Task extends PanacheEntity {

    public String description;
    public boolean done;
    public Date doneDate;

    /**
     * Creates and persists a new task with the given description.
     * 
     * The new task is created in an incomplete state (done = false)
     * and is immediately persisted to the database.
     * 
     * @param description The task description (what needs to be done)
     * @return The newly created and persisted Task entity
     */
    public static Task add(String description) {
        Task task = new Task();
        task.description = description;
        task.done = false;
        task.persist();
        return task;
    }

    /**
     * Retrieves all tasks ordered by their completion date.
     * 
     * This method returns all tasks in the database, sorted by doneDate:
     * - Incomplete tasks (doneDate = null) appear first
     * - Completed tasks are ordered by their doneDate in ascending order (oldest
     * first)
     * 
     * This ordering is useful for displaying tasks in a logical sequence where
     * pending tasks are shown first, followed by completed tasks in chronological
     * order.
     * 
     * @return List of all tasks ordered by doneDate (nulls first, then ascending)
     */
    public static List<Task> findAllOrderByDoneDate() {
        return find("ORDER BY doneDate ASC NULLS FIRST").list();
    }

}