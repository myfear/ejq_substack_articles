package org.acme.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionChatMemoryStore implements ChatMemoryStore {

    // Thread-safe storage for session messages
    private final Map<String, List<ChatMessage>> sessionMessages = new ConcurrentHashMap<>();
    private final Map<String, ReadWriteLock> sessionLocks = new ConcurrentHashMap<>();

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String sessionId = memoryId.toString();
        Log.infof("Getting messages for session: %s", sessionId);

        ReadWriteLock lock = getSessionLock(sessionId);

        lock.readLock().lock();
        try {
            List<ChatMessage> messages = sessionMessages.getOrDefault(sessionId, Collections.emptyList());
            Log.infof("Retrieved %d messages for session: %s", messages.size(), sessionId);
            return new ArrayList<>(messages);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String sessionId = memoryId.toString();
        Log.infof("Updating messages for session: %s, message count: %d", sessionId, messages.size());

        ReadWriteLock lock = getSessionLock(sessionId);

        lock.writeLock().lock();
        try {
            sessionMessages.put(sessionId, new ArrayList<>(messages));
            Log.infof("Updated messages for session: %s. Total sessions in store: %d",
                    sessionId, sessionMessages.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String sessionId = memoryId.toString();
        Log.infof("Delete request for session: %s - PRESERVING session but keeping messages for LLM memory", sessionId);

        // DO NOT delete the session or clear messages - this preserves conversation
        // history for LLM
        // The AI framework calls this after each conversation turn, but we want to keep
        // both:
        // 1. The session entry (for /api/sessions endpoint)
        // 2. The message history (for LLM context)

        // Simply log that we're preserving everything
        ReadWriteLock lock = getSessionLock(sessionId);
        lock.readLock().lock();
        try {
            int currentMessages = sessionMessages.getOrDefault(sessionId, Collections.emptyList()).size();
            Log.infof("Preserving session: %s with %d messages for continued LLM context. Total sessions: %d",
                    sessionId, currentMessages, sessionMessages.size());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Get all active sessions
     */
    public Set<String> getActiveSessions() {
        Set<String> sessions = new HashSet<>(sessionMessages.keySet());
        Log.infof("getActiveSessions() called. Found %d active sessions: %s", sessions.size(), sessions);
        return sessions;
    }

    /**
     * Get message count for a session
     */
    public int getMessageCount(String sessionId) {
        ReadWriteLock lock = getSessionLock(sessionId);
        lock.readLock().lock();
        try {
            int count = sessionMessages.getOrDefault(sessionId, Collections.emptyList()).size();
            Log.debugf("Message count for session %s: %d", sessionId, count);
            return count;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Actually delete a session and its memory (used by API endpoints)
     */
    public void forceDeleteSession(String sessionId) {
        Log.infof("Force deleting session: %s", sessionId);

        ReadWriteLock lock = getSessionLock(sessionId);
        lock.writeLock().lock();
        try {
            sessionMessages.remove(sessionId);
            sessionLocks.remove(sessionId);
            Log.infof("Force deleted session: %s. Remaining sessions: %d", sessionId, sessionMessages.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clear all sessions (useful for testing or maintenance)
     */
    public void clearAllSessions() {
        Log.infof("Clearing all sessions. Current count: %d", sessionMessages.size());
        sessionMessages.clear();
        sessionLocks.clear();
        Log.info("All sessions cleared");
    }

    private ReadWriteLock getSessionLock(String sessionId) {
        return sessionLocks.computeIfAbsent(sessionId, k -> {
            Log.debugf("Creating new lock for session: %s", sessionId);
            return new ReentrantReadWriteLock();
        });
    }
}
