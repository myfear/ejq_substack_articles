package org.acme.todo;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Todo extends PanacheEntity {

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    public String title;

    @Column(nullable = false)
    public boolean done = false;
}