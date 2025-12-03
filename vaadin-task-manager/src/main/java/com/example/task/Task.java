package com.example.task;

import java.time.LocalDate;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Task extends PanacheEntity {

    @Column(nullable = false)
    public String title;

    @Column(length = 1024)
    public String description;

    public LocalDate dueDate;

    public boolean done;
}