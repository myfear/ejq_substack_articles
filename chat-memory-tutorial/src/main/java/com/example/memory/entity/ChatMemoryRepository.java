package com.example.memory.entity;

import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ChatMemoryRepository implements PanacheRepository<ChatMessageEntity> {

    @Transactional(Transactional.TxType.MANDATORY)
    public List<ChatMessageEntity> findByMemoryId(String memoryId) {
        return find("memoryId = ?1 ORDER BY createdAt", memoryId).list();
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public void save(ChatMessageEntity entity) {
        if (entity.createdAt == null) {
            entity.createdAt = java.time.Instant.now();
        }
        // Use merge which works for both insert and update
        getEntityManager().merge(entity);
        getEntityManager().flush();
    }

    @Transactional(Transactional.TxType.MANDATORY)
    public void deleteByMemoryId(String memoryId) {
        delete("memoryId", memoryId);
    }
}