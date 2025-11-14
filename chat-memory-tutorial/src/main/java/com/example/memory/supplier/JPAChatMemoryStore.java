package com.example.memory.supplier;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.example.memory.ChatMemoryEntity;
import com.example.memory.ChatMemoryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkus.logging.Log;

@ApplicationScoped
@Priority(201)
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
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            Log.warnf("No messages to update for memory ID: %s", memoryId);
            return;
        }

        String memoryIdString = memoryId.toString();
        try {
            // Serialize messages to JSON and then to byte array
            byte[] messagesBytes = objectMapper.writeValueAsBytes(messages);

            // Load existing entity or create new one
            ChatMemoryEntity entity = repository.load(memoryIdString);
            if (entity == null) {
                entity = new ChatMemoryEntity();
                entity.memoryId = memoryIdString;
            }

            // Update entity with new messages
            entity.messages = messagesBytes;
            entity.messageCount = messages.size();

            // Save to database (repository.save() will update lastUpdated)
            repository.save(entity);
            Log.infof("Updated messages for memory ID: %s with %d messages", memoryIdString, messages.size());
        } catch (Exception e) {
            Log.errorf(e, "Failed to update messages for memory ID: %s", memoryIdString);
            throw new RuntimeException("Failed to update messages", e);
        }
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String memoryIdString = memoryId.toString();
        try {
            ChatMemoryEntity entity = repository.load(memoryIdString);
            if (entity == null || entity.messages == null || entity.messages.length == 0) {
                Log.debugf("No messages found for memory ID: %s", memoryIdString);
                return new ArrayList<>();
            }

            // Deserialize byte array to List<ChatMessage>
            List<ChatMessage> messages = objectMapper.readValue(
                    entity.messages,
                    new TypeReference<List<ChatMessage>>() {
                    });
            Log.debugf("Retrieved %d messages for memory ID: %s", messages.size(), memoryIdString);
            return messages;
        } catch (Exception e) {
            Log.errorf(e, "Failed to get messages for memory ID: %s", memoryIdString);
            throw new RuntimeException("Failed to get messages", e);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String memoryIdString = memoryId.toString();
        try {
            ChatMemoryEntity entity = repository.load(memoryIdString);
            if (entity != null) {
                repository.delete(entity);
                Log.infof("Deleted messages for memory ID: %s", memoryIdString);
            } else {
                Log.debugf("No messages to delete for memory ID: %s", memoryIdString);
            }
        } catch (Exception e) {
            Log.errorf(e, "Failed to delete messages for memory ID: %s", memoryIdString);
            throw new RuntimeException("Failed to delete messages", e);
        }
    }
}

