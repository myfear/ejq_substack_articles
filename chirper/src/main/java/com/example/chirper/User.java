package com.example.chirper;

import java.time.LocalDateTime;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {
    @Column(unique = true, nullable = false)
    public String username;

    @Column(nullable = false)
    public String displayName;

    public String bio;

    @Column(nullable = false)
    public LocalDateTime createdAt;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    public List<Chirp> chirps;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }
}
