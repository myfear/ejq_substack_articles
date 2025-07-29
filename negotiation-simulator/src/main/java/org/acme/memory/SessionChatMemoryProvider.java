package org.acme.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SessionChatMemoryProvider implements ChatMemoryProvider {

    @Inject
    SessionChatMemoryStore memoryStore;

    // Configuration for memory window size
    private static final int DEFAULT_MAX_MESSAGES = 50;

    @Override
    public ChatMemory get(Object memoryId) {
        String sessionId = memoryId.toString();
        Log.infof("Creating ChatMemory for session: %s with max messages: %d", sessionId, DEFAULT_MAX_MESSAGES);

        ChatMemory memory = MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(DEFAULT_MAX_MESSAGES)
                .chatMemoryStore(memoryStore)
                .build();

        Log.infof("ChatMemory created successfully for session: %s", sessionId);
        return memory;
    }
}
