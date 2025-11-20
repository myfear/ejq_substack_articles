package com.example.memory.store.jpa;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.memory.entity.ChatMessageEntity;
import com.example.memory.entity.ChatMemoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkus.arc.Unremovable;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Typed(JPAChatMemoryStore.class)
@Unremovable
public class JPAChatMemoryStore implements ChatMemoryStore {

    /**
     * The repository used to persist and retrieve chat messages from the database.
     */
    private final ChatMemoryRepository repository;

    /**
     * The ObjectMapper used for serializing and deserializing ChatMessage objects.
     */
    private final ObjectMapper objectMapper;

    @Inject
    public JPAChatMemoryStore(ChatMemoryRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            Log.warnf("No messages to update for memory ID: %s", memoryId);
            return;
        }

        String memoryIdString = memoryId.toString();
        try {
            // Delete existing messages for this memory ID
            repository.deleteByMemoryId(memoryIdString);

            // Save each message as a separate entity
            // Use incremental timestamps to preserve message order
            Instant baseTime = Instant.now();
            for (int i = 0; i < messages.size(); i++) {
                ChatMessage message = messages.get(i);
                ChatMessageEntity entity = new ChatMessageEntity();
                entity.memoryId = memoryIdString;
                entity.type = message.type().toString();
                // Serialize the full ChatMessage to JSON for the text field
                entity.text = objectMapper.writeValueAsString(message);
                // Use incremental timestamps to maintain order (millisecond precision)
                entity.createdAt = baseTime.plusMillis(i);
                repository.save(entity);
            }

            Log.infof("Updated messages for memory ID: %s with %d messages", memoryIdString, messages.size());
        } catch (Exception e) {
            Log.errorf(e, "Failed to update messages for memory ID: %s", memoryIdString);
            throw new RuntimeException("Failed to update messages", e);
        }
    }

    @Override
    @Transactional
    public List<ChatMessage> getMessages(Object memoryId) {
        String memoryIdString = memoryId.toString();
        try {
            List<ChatMessageEntity> entities = repository.findByMemoryId(memoryIdString);
            if (entities == null || entities.isEmpty()) {
                Log.debugf("No messages found for memory ID: %s", memoryIdString);
                return new ArrayList<>();
            }

            // Deserialize each entity back to ChatMessage
            List<ChatMessage> messages = entities.stream()
                    .map(entity -> {
                        try {
                            return objectMapper.readValue(entity.text, ChatMessage.class);
                        } catch (Exception e) {
                            Log.errorf(e, "Failed to deserialize message entity with ID: %d", entity.id);
                            throw new RuntimeException("Failed to deserialize message", e);
                        }
                    })
                    .collect(Collectors.toList());

            Log.debugf("Retrieved %d messages for memory ID: %s", messages.size(), memoryIdString);
            return messages;
        } catch (Exception e) {
            Log.errorf(e, "Failed to get messages for memory ID: %s", memoryIdString);
            throw new RuntimeException("Failed to get messages", e);
        }
    }

    @Override
    @Transactional
    public void deleteMessages(Object memoryId) {
        String memoryIdString = memoryId.toString();
        try {
            repository.deleteByMemoryId(memoryIdString);
            Log.infof("Deleted messages for memory ID: %s", memoryIdString);
        } catch (IllegalStateException e) {
            // EntityManagerFactory might be closed during shutdown - this is expected
            if (e.getMessage() != null && e.getMessage().contains("EntityManagerFactory is closed")) {
                Log.debugf("Skipping delete for memory ID: %s - EntityManagerFactory is closed (shutdown in progress)", memoryIdString);
                return;
            }
            // Re-throw if it's a different IllegalStateException
            Log.errorf(e, "Failed to delete messages for memory ID: %s", memoryIdString);
            throw new RuntimeException("Failed to delete messages", e);
        } catch (Exception e) {
            Log.errorf(e, "Failed to delete messages for memory ID: %s", memoryIdString);
            throw new RuntimeException("Failed to delete messages", e);
        }
    }
}

