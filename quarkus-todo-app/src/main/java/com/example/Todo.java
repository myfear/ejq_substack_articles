package com.example;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Todo extends PanacheEntity {
    public String title;
    public boolean completed;
}
