package com.example.model;

import java.time.OffsetDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "post")
public class PostEntity extends PanacheEntity {
    public String uri;
    public String text;
    public OffsetDateTime createdAt;
    public int hourOfDay;
    public String hashtags;
    public String links;
    public String frameworks;
    public String language;
    public OffsetDateTime indexedAt;
}
