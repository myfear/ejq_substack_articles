package com.example.auth;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_groups")
public class GroupEntity extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String name;
}