package com.example.memory.entity;

import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_message", indexes = @Index(name = "idx_memory_id", columnList = "memory_id")) 
public class ChatMessageEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id; // Auto-generated ID

    @Column(name = "memory_id") // Indexed for fast lookups
    public String memoryId;

    public String type; // "USER", "AI", "SYSTEM"

    @Lob
    public String text; // The message content

    public Instant createdAt;
}

