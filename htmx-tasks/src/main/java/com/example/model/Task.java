package com.example.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Task extends PanacheEntity {

    @NotBlank
    @Size(min = 3, max = 100)
    public String title;

    public boolean completed;
}