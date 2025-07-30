package com.example.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Dealership extends PanacheEntity {
    public String name;
    public String city;
}