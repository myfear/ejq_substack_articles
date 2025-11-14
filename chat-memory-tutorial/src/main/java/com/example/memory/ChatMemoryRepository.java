package com.example.memory;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChatMemoryRepository implements PanacheRepository<ChatMemoryEntity> {

    public ChatMemoryEntity load(String chatMessageId) {
        return find("memoryId", chatMessageId).firstResult();
    }

    public void save(ChatMemoryEntity entity) {
        entity.lastUpdated = java.time.Instant.now();
        // Use merge which works for both insert and update
        getEntityManager().merge(entity);
        getEntityManager().flush();
    }
}