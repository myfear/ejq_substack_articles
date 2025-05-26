package org.acme.todo;

import org.hibernate.annotations.TenantId;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "todos")
public class Todo extends PanacheEntity {
    public String title;
    public boolean completed = false;

    @TenantId
    public String tenantId;

    public Todo() {
    }

    public Todo(String title, String tenantId) {
        this.title = title;
        this.tenantId = tenantId;
    }
}