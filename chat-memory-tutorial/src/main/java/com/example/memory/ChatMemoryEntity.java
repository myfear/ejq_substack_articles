package com.example.memory;

import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_memory")
public class ChatMemoryEntity extends PanacheEntityBase {


    @Id
    @Column(name = "memory_id", nullable = false, length = 255)
    public String memoryId;

    @Lob
    @Column(nullable = false)
    public byte[] messages;

    @Lob
    public String summary;

    public Integer messageCount;

    public Instant lastUpdated = Instant.now();
}