package com.example.entity;

import com.example.model.PostStatus;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "post")
public class Post extends PanacheEntity {

    @Column(length = 250)
    public String title;

    @Column(columnDefinition = "NUMERIC(3)")
    @Enumerated(EnumType.ORDINAL)
    public PostStatus status;

    public Post setId(Long id) {
        this.id = id;
        return this;
    }

    public Post setTitle(String title) {
        this.title = title;
        return this;
    }

    public Post setStatus(PostStatus status) {
        this.status = status;
        return this;
    }
}