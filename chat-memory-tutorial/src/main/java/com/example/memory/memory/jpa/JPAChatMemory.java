package com.example.memory.memory.jpa;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.ConfigProvider;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

/**
 * A ChatMemory implementation that stores all chat messages persistently
 * using JPAChatMemoryStore.
 * 
 * This implementation stores all messages in the database but applies a
 * sliding window when retrieving messages, preserving the system message
 * and returning the most recent N messages.
 */
public class JPAChatMemory implements ChatMemory {

    private final Object id;
    private final ChatMemoryStore chatMemoryStore;
    private final int maxMessages; // Used for sliding window

    private JPAChatMemory(Builder builder) {
        this.id = builder.id;
        this.chatMemoryStore = builder.chatMemoryStore;
        this.maxMessages = builder.getMaxMessages();
    }

    /**
     * Creates a new builder for JPAChatMemory.
     * 
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public Object id() {
        return id;
    }

    @Override
    public void add(ChatMessage message) {
        if (message == null) {
            return;
        }

        // Get current messages from the store
        List<ChatMessage> currentMessages = chatMemoryStore.getMessages(id);

        // Create a new list with the new message added
        List<ChatMessage> updatedMessages = new ArrayList<>(currentMessages);
        updatedMessages.add(message);

        // Update the store with all messages (store all, windowing happens on
        // retrieval)
        chatMemoryStore.updateMessages(id, updatedMessages);
    }

    @Override
    public List<ChatMessage> messages() {
        // Fetch all messages from the store
        List<ChatMessage> allMessages = chatMemoryStore.getMessages(id);

        if (allMessages.isEmpty()) {
            return allMessages;
        }

        // Find the SystemMessage (usually the first one)
        SystemMessage systemMessage = null;
        List<ChatMessage> nonSystemMessages = new ArrayList<>();

        for (ChatMessage message : allMessages) {
            if (message.type() == ChatMessageType.SYSTEM) {
                if (systemMessage == null) {
                    systemMessage = (SystemMessage) message;
                }
            } else {
                nonSystemMessages.add(message);
            }
        }

        // Select the Recent N messages (Sliding Window)
        int messagesToTake = Math.min(maxMessages, nonSystemMessages.size());
        List<ChatMessage> recentMessages = nonSystemMessages.isEmpty()
                ? new ArrayList<>()
                : nonSystemMessages.subList(
                        Math.max(0, nonSystemMessages.size() - messagesToTake),
                        nonSystemMessages.size());

        // Combine: [System Message] + [Last N Messages]
        List<ChatMessage> result = new ArrayList<>();
        if (systemMessage != null) {
            result.add(systemMessage);
        }
        result.addAll(recentMessages);

        return result;
    }

    @Override
    public void clear() {
        chatMemoryStore.deleteMessages(id);
    }

    /**
     * Builder for creating JPAChatMemory instances.
     * Applies sliding window logic when retrieving messages.
     */
    public static class Builder {
        private Object id;
        private ChatMemoryStore chatMemoryStore;
        private Integer maxMessages; // Sliding window size, read from config if not set

        /**
         * Gets the maxMessages value, reading from configuration if not explicitly set.
         * 
         * @return the maxMessages value from config or default of 20
         */
        private int getMaxMessages() {
            if (maxMessages == null) {
                return ConfigProvider.getConfig()
                        .getOptionalValue("chat-memory.max-messages", Integer.class)
                        .orElse(20);
            }
            return maxMessages;
        }

        /**
         * Sets the memory ID for this chat memory instance.
         * 
         * @param id the memory ID (typically a session identifier)
         * @return this builder
         */
        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the ChatMemoryStore to use for persistence.
         * 
         * @param chatMemoryStore the store implementation
         * @return this builder
         */
        public Builder chatMemoryStore(ChatMemoryStore chatMemoryStore) {
            this.chatMemoryStore = chatMemoryStore;
            return this;
        }

        /**
         * Sets the maximum number of recent messages to return (sliding window size).
         * The system message is always preserved and not counted in this limit.
         * 
         * @param maxMessages the maximum number of recent messages to return
         * @return this builder
         */
        public Builder maxMessages(int maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        /**
         * Builds a new JPAChatMemory instance.
         * 
         * @return a new JPAChatMemory instance
         * @throws IllegalStateException if required fields are not set
         */
        public JPAChatMemory build() {
            if (id == null) {
                throw new IllegalStateException("id must be set");
            }
            if (chatMemoryStore == null) {
                throw new IllegalStateException("chatMemoryStore must be set");
            }
            return new JPAChatMemory(this);
        }
    }
}