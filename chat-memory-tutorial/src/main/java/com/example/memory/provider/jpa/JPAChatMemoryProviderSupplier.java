package com.example.memory.provider.jpa;

import java.util.function.Supplier;

import org.eclipse.microprofile.config.ConfigProvider;

import com.example.memory.memory.jpa.JPAChatMemory;
import com.example.memory.store.jpa.JPAChatMemoryStore;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import jakarta.enterprise.inject.spi.CDI;

/**
 * Supplier that provides a ChatMemoryProvider using JPAChatMemoryStore
 * for persistent storage of chat messages in the database.
 * 
 * This can be used with @RegisterAiService annotation:
 * 
 * @RegisterAiService(chatMemoryProviderSupplier =
 *                                               JPAChatMemoryProviderSupplier.class)
 */

public class JPAChatMemoryProviderSupplier implements Supplier<ChatMemoryProvider> {

    private final JPAChatMemoryStore chatMemoryStore;
    private final int maxMessages;

    public JPAChatMemoryProviderSupplier() {
        // Retrieve the managed JPAChatMemoryStore instance from the CDI container.
        this.chatMemoryStore = CDI.current().select(JPAChatMemoryStore.class).get();
        // Get maxMessages from config or use default
        this.maxMessages = ConfigProvider.getConfig()
                .getOptionalValue("chat-memory.max-messages", Integer.class)
                .orElse(20);
    }

    @Override
    public ChatMemoryProvider get() {
        return new ChatMemoryProvider() {
            @Override
            public ChatMemory get(Object memoryId) {
                return JPAChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(maxMessages)
                        .chatMemoryStore(chatMemoryStore)
                        .build();
            }
        };
    }
}