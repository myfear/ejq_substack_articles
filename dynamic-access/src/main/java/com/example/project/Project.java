package com.example.project;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "projects")
public class Project extends PanacheEntity {
    public String name;
    public String description;
}