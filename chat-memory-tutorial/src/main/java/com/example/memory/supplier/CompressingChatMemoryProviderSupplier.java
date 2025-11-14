package com.example.memory.supplier;

import java.util.function.Supplier;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * Supplier that provides a ChatMemoryProvider using CompressingChatMemoryStore
 * for semantic compression of chat messages when they exceed a threshold.
 * 
 * This can be used with @RegisterAiService annotation:
 * 
 * @RegisterAiService(chatMemoryProviderSupplier =
 *                                               CompressingChatMemoryProviderSupplier.class)
 */
@ApplicationScoped
public class CompressingChatMemoryProviderSupplier implements Supplier<ChatMemoryProvider> {

    private final CompressingChatMemoryStore chatMemoryStore;

    public CompressingChatMemoryProviderSupplier() {
        this.chatMemoryStore = null;
    }

    @Inject
    public CompressingChatMemoryProviderSupplier(CompressingChatMemoryStore chatMemoryStore) {
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
