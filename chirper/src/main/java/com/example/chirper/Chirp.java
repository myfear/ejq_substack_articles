package com.example.chirper;

import java.time.LocalDateTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "chirps")
public class Chirp extends PanacheEntity {
    @Column(nullable = false, length = 280)
    public String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    public User author;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @Column()
    public int likes = 0;

    @Column()
    public int rechirps = 0;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static List<Chirp> findAllOrderedByDate() {
        return list("ORDER BY createdAt DESC");
    }

    public static List<Chirp> findByAuthor(User author) {
        return list("author", author);
    }
}
