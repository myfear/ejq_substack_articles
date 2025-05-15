package org.acme.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class Post extends PanacheEntity {

    @Column(nullable = false)
    public String title;

    @Column(columnDefinition = "TEXT")
    public String content;

    public String authorEmail;
    public LocalDateTime creationDate;
    public LocalDateTime lastModifiedDate;

    public Post() {}

    public Post(String title, String content, String authorEmail) {
        this.title = title;
        this.content = content;
        this.authorEmail = authorEmail;
        this.creationDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }
}
