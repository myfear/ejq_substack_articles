package com.example.memory.service;

import com.example.memory.provider.jpa.JPAChatMemoryProviderSupplier;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService(chatMemoryProviderSupplier = JPAChatMemoryProviderSupplier.class)
@ApplicationScoped
public interface JPAMemoryBot {
    @SystemMessage("""
        You are a polite and helpful assistant.
        """)
    String chat(@MemoryId String memoryId, @UserMessage String message);
}