package com.example.memory.aiservices;

import com.example.memory.supplier.CompressingChatMemoryProviderSupplier;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(chatMemoryProviderSupplier = CompressingChatMemoryProviderSupplier.class)
@SystemMessage("""
        You are a polite and helpful assistant.
        """)
public interface CompressedMemoryBot {
    String chat(@MemoryId String memoryId, @UserMessage String message);
}
