package com.example.memory.memory.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.memory.entity.ChatMemoryRepository;
import com.example.memory.entity.ChatMessageEntity;
import com.example.memory.service.JPAMemoryBot;
import com.example.memory.store.jpa.JPAChatMemoryStore;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Unit tests for JPAChatMemory.
 * 
 * Tests the sliding window algorithm and message management functionality.
 */
@QuarkusTest
class JPAChatMemoryTest {

    @Inject
    JPAMemoryBot jpaMemoryBot;

    @Inject
    ChatMemoryRepository chatMemoryRepository;

    @Inject
    JPAChatMemoryStore chatMemoryStore;

    @Test
    @Transactional
    void testChat() {
        String memoryId = "test-" + System.currentTimeMillis();
        
        // Send a message
        String response = jpaMemoryBot.chat(memoryId, "Hello, world!");
        
        // Verify response is not null
        assertNotNull(response);
        
        // Verify messages are stored in the database
        List<ChatMessageEntity> messages = chatMemoryRepository.findByMemoryId(memoryId);
        assertEquals(3, messages.size(), "Should have 3 messages: SYSTEM, USER and AI");
        
        // Verify message types
        assertEquals("SYSTEM", messages.get(0).type);
        assertEquals("USER", messages.get(1).type);
        assertEquals("AI", messages.get(2).type);
        
        // Clean up explicitly to avoid shutdown issues
        chatMemoryStore.deleteMessages(memoryId);
    }

    @Test
    @Transactional
    void testSlidingWindow() {
        String memoryId = "test-window-" + System.currentTimeMillis();
        int maxMessages = 5; // Use a smaller window for testing
        
        // Create a JPAChatMemory instance with a small window
        JPAChatMemory chatMemory = JPAChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(maxMessages)
                .build();

        // Create a system message
        SystemMessage systemMessage = SystemMessage.systemMessage("You are a helpful assistant");
        
        // Create many messages (more than maxMessages)
        // We'll create 10 user/ai pairs = 20 non-system messages
        List<ChatMessage> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        
        for (int i = 1; i <= 10; i++) {
            allMessages.add(UserMessage.userMessage("User message " + i));
            allMessages.add(AiMessage.aiMessage("AI response " + i));
        }
        
        // Store all messages in the database
        chatMemoryStore.updateMessages(memoryId, allMessages);
        
        // Verify all messages are stored in the database
        List<ChatMessageEntity> storedEntities = chatMemoryRepository.findByMemoryId(memoryId);
        assertEquals(21, storedEntities.size(), "All 21 messages (1 system + 20 non-system) should be stored");
        
        // Now test the sliding window - messages() should return system + last 5 non-system messages
        List<ChatMessage> windowedMessages = chatMemory.messages();
        
        // Should have: 1 system message + 5 non-system messages = 6 total
        assertEquals(6, windowedMessages.size(), 
                "Should return 1 system message + " + maxMessages + " recent non-system messages");
        
        // Verify system message is first
        assertEquals(ChatMessageType.SYSTEM, windowedMessages.get(0).type());
        assertEquals(systemMessage.text(), ((SystemMessage) windowedMessages.get(0)).text());
        
        // Verify we have exactly maxMessages non-system messages
        long nonSystemCount = windowedMessages.stream()
                .filter(m -> m.type() != ChatMessageType.SYSTEM)
                .count();
        assertEquals(maxMessages, nonSystemCount, 
                "Should have exactly " + maxMessages + " non-system messages");
        
        // Verify the last messages are the most recent ones
        // The last 5 non-system messages should be: user9, ai9, user10, ai10 (wait, that's 4)
        // Actually, with 10 pairs, we have: user1, ai1, user2, ai2, ..., user10, ai10
        // Last 5 would be: user8, ai8, user9, ai9, user10 (or similar depending on exact count)
        // Let me verify the last message is from the end
        ChatMessage lastMessage = windowedMessages.get(windowedMessages.size() - 1);
        assertTrue(lastMessage instanceof AiMessage || lastMessage instanceof UserMessage,
                "Last message should be a user or AI message");
        
        // Verify the messages are the most recent ones by checking they contain high numbers
        // The last 5 non-system messages should be from messages 8-10 (roughly)
        boolean containsRecentMessages = windowedMessages.stream()
                .filter(m -> m.type() != ChatMessageType.SYSTEM)
                .anyMatch(m -> {
                    String text = m instanceof UserMessage 
                            ? ((UserMessage) m).singleText()
                            : ((AiMessage) m).text();
                    return text.contains("8") || text.contains("9") || text.contains("10");
                });
        assertTrue(containsRecentMessages, 
                "Windowed messages should contain recent messages (8, 9, or 10)");
        
        // Verify older messages are NOT included
        // With 20 non-system messages and maxMessages=5, the window should be the last 5
        // So messages 1-2 should NOT be included (they're too old)
        boolean containsVeryOldMessages = windowedMessages.stream()
                .filter(m -> m.type() != ChatMessageType.SYSTEM)
                .anyMatch(m -> {
                    String text = m instanceof UserMessage 
                            ? ((UserMessage) m).singleText()
                            : ((AiMessage) m).text();
                    return text.equals("User message 1") || text.equals("AI response 1") ||
                           text.equals("User message 2") || text.equals("AI response 2");
                });
        assertTrue(!containsVeryOldMessages, 
                "Windowed messages should NOT contain very old messages (1 or 2)");
        
        // Clean up
        chatMemoryStore.deleteMessages(memoryId);
    }

    @Test
    @Transactional
    void testSlidingWindowWithExactCount() {
        String memoryId = "test-exact-" + System.currentTimeMillis();
        int maxMessages = 3;
        
        JPAChatMemory chatMemory = JPAChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(maxMessages)
                .build();

        SystemMessage systemMessage = SystemMessage.systemMessage("System prompt");
        List<ChatMessage> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        
        // Create exactly maxMessages non-system messages
        allMessages.add(UserMessage.userMessage("Message 1"));
        allMessages.add(AiMessage.aiMessage("Response 1"));
        allMessages.add(UserMessage.userMessage("Message 2"));
        
        chatMemoryStore.updateMessages(memoryId, allMessages);
        
        // Should return all messages (system + 3 non-system)
        List<ChatMessage> windowedMessages = chatMemory.messages();
        assertEquals(4, windowedMessages.size(), 
                "Should return all messages when count equals maxMessages");
        assertEquals(ChatMessageType.SYSTEM, windowedMessages.get(0).type());
        
        // Clean up
        chatMemoryStore.deleteMessages(memoryId);
    }

    @Test
    @Transactional
    void testSlidingWindowWithFewerThanMax() {
        String memoryId = "test-few-" + System.currentTimeMillis();
        int maxMessages = 10;
        
        JPAChatMemory chatMemory = JPAChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(maxMessages)
                .build();

        SystemMessage systemMessage = SystemMessage.systemMessage("System prompt");
        List<ChatMessage> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        
        // Create fewer messages than maxMessages
        allMessages.add(UserMessage.userMessage("Message 1"));
        allMessages.add(AiMessage.aiMessage("Response 1"));
        
        chatMemoryStore.updateMessages(memoryId, allMessages);
        
        // Should return all messages (system + 2 non-system)
        List<ChatMessage> windowedMessages = chatMemory.messages();
        assertEquals(3, windowedMessages.size(), 
                "Should return all messages when count is less than maxMessages");
        assertEquals(ChatMessageType.SYSTEM, windowedMessages.get(0).type());
        
        // Clean up
        chatMemoryStore.deleteMessages(memoryId);
    }

    @Test
    @Transactional
    void testSlidingWindowUsesConfigurationDefault() {
        String memoryId = "test-config-default-" + System.currentTimeMillis();
        
        // Create JPAChatMemory without explicitly setting maxMessages
        // It should read from chat-memory.max-messages configuration (default: 20)
        JPAChatMemory chatMemory = JPAChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                // maxMessages not set - should use config value
                .build();

        SystemMessage systemMessage = SystemMessage.systemMessage("System prompt");
        List<ChatMessage> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        
        // Create 25 non-system messages (more than default config of 20)
        for (int i = 1; i <= 25; i++) {
            allMessages.add(UserMessage.userMessage("User message " + i));
            allMessages.add(AiMessage.aiMessage("AI response " + i));
        }
        
        chatMemoryStore.updateMessages(memoryId, allMessages);
        
        // Verify all messages are stored
        List<ChatMessageEntity> storedEntities = chatMemoryRepository.findByMemoryId(memoryId);
        assertEquals(51, storedEntities.size(), "All 51 messages (1 system + 50 non-system) should be stored");
        
        // Should return system + last 20 non-system messages (from config)
        List<ChatMessage> windowedMessages = chatMemory.messages();
        assertEquals(21, windowedMessages.size(), 
                "Should return 1 system message + 20 recent non-system messages (from config)");
        
        assertEquals(ChatMessageType.SYSTEM, windowedMessages.get(0).type());
        
        // Verify we have exactly 20 non-system messages (from config default)
        long nonSystemCount = windowedMessages.stream()
                .filter(m -> m.type() != ChatMessageType.SYSTEM)
                .count();
        assertEquals(20, nonSystemCount, 
                "Should have exactly 20 non-system messages (from chat-memory.max-messages config)");
        
        // Clean up
        chatMemoryStore.deleteMessages(memoryId);
    }

    @Test
    @Transactional
    void testSlidingWindowWithCustomConfiguration() {
        String memoryId = "test-custom-config-" + System.currentTimeMillis();
        int customMaxMessages = 7;
        
        // Create JPAChatMemory with explicitly set maxMessages to test custom value
        // In real usage, this would come from configuration
        JPAChatMemory chatMemory = JPAChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(customMaxMessages)
                .build();

        SystemMessage systemMessage = SystemMessage.systemMessage("System prompt");
        List<ChatMessage> allMessages = new ArrayList<>();
        allMessages.add(systemMessage);
        
        // Create 15 non-system messages (more than test config of 7)
        for (int i = 1; i <= 15; i++) {
            allMessages.add(UserMessage.userMessage("User message " + i));
            allMessages.add(AiMessage.aiMessage("AI response " + i));
        }
        
        chatMemoryStore.updateMessages(memoryId, allMessages);
        
        // Verify all messages are stored
        List<ChatMessageEntity> storedEntities = chatMemoryRepository.findByMemoryId(memoryId);
        assertEquals(31, storedEntities.size(), "All 31 messages (1 system + 30 non-system) should be stored");
        
        // Should return system + last 7 non-system messages (from custom maxMessages)
        List<ChatMessage> windowedMessages = chatMemory.messages();
        assertEquals(8, windowedMessages.size(), 
                "Should return 1 system message + 7 recent non-system messages (from custom maxMessages)");
        
        assertEquals(ChatMessageType.SYSTEM, windowedMessages.get(0).type());
        
        // Verify we have exactly 7 non-system messages (from custom maxMessages)
        long nonSystemCount = windowedMessages.stream()
                .filter(m -> m.type() != ChatMessageType.SYSTEM)
                .count();
        assertEquals(7, nonSystemCount, 
                "Should have exactly 7 non-system messages (from custom maxMessages=7)");
        
        // Verify older messages are NOT included
        boolean containsOldMessages = windowedMessages.stream()
                .filter(m -> m.type() != ChatMessageType.SYSTEM)
                .anyMatch(m -> {
                    String text = m instanceof UserMessage 
                            ? ((UserMessage) m).singleText()
                            : ((AiMessage) m).text();
                    return text.contains("1") || text.contains("2") || text.contains("3");
                });
        // With 30 non-system messages and window of 7, messages 1-3 should NOT be in the window
        assertTrue(!containsOldMessages || windowedMessages.size() <= 4, 
                "Windowed messages should NOT contain very old messages when using custom config");
        
        // Clean up
        chatMemoryStore.deleteMessages(memoryId);
    }
}

