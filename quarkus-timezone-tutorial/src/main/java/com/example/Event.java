package com.example;

import java.time.OffsetDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Event extends PanacheEntity {
    public String name;
    public OffsetDateTime eventTimestamp;
}