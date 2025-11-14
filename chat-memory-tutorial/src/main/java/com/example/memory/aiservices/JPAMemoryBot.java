package com.example.memory.aiservices;

import com.example.memory.supplier.JPAChatMemoryProviderSupplier;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService(chatMemoryProviderSupplier = JPAChatMemoryProviderSupplier.class)
@ApplicationScoped
public interface JPAMemoryBot {
    String chat(@MemoryId String memoryId, @UserMessage String message);
}
