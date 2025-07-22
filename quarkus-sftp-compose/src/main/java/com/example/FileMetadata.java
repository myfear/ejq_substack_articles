package com.example;

import java.time.LocalDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class FileMetadata extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String fileName;

    public long fileSize;

    public LocalDateTime uploadTimestamp;
}