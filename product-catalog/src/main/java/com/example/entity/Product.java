package com.example.entity;

import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_category_views_id", columnList = "category, view_count DESC, id"),
        @Index(name = "idx_category_created_id", columnList = "category, created_at DESC, id")
})
public class Product extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(length = 1000)
    public String description;

    @Column(nullable = false)
    public String category;

    @Column(nullable = false)
    public Double price;

    @Column(name = "view_count", nullable = false)
    public Integer viewCount = 0;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "image_url")
    public String imageUrl;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}