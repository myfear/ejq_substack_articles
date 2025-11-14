package com.example.memory.supplier;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * Supplier that provides a ChatMemoryProvider using JPAChatMemoryStore
 * for persistent storage of chat messages in the database.
 * 
 * This can be used with @RegisterAiService annotation:
 * @RegisterAiService(chatMemoryProviderSupplier = JPAChatMemoryProviderSupplier.class)
 */
@ApplicationScoped
public class JPAChatMemoryProviderSupplier implements Supplier<ChatMemoryProvider> {

    private final JPAChatMemoryStore chatMemoryStore;

    public JPAChatMemoryProviderSupplier() {
        this.chatMemoryStore = null;
    }

    @Inject
    public JPAChatMemoryProviderSupplier(JPAChatMemoryStore chatMemoryStore) {
        this.chatMemoryStore = chatMemoryStore;
    }

    @Override
    public ChatMemoryProvider get() {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .chatMemoryStore(chatMemoryStore)
                        .build();
            }
        };
    }
}

