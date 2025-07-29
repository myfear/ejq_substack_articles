package org.acme;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.acme.memory.SessionChatMemoryStore;
import org.acme.model.Message;
import org.acme.model.PersonalityType;
import org.acme.model.Scenario;
import org.acme.model.Session;
import org.acme.service.FeedbackAssistant;
import org.acme.service.ManagerAssistant;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * REST API for the negotiation simulator providing endpoints for:
 * - Session management (create sessions)
 * - Conversation handling (send messages, get feedback)
 * - Configuration (get personalities and scenarios)
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionResource {

    private static final String USER_ROLE = "user";
    private static final String AI_ROLE = "ai";

    // In-memory storage for active sessions
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @Inject
    ManagerAssistant managerAssistant;

    @Inject
    FeedbackAssistant feedbackAssistant;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SessionChatMemoryStore memoryStore;

    // Configuration endpoints

    /**
     * Get all available personality types for managers.
     */
    @GET
    @Path("/personalities")
    public PersonalityType[] getPersonalities() {
        return PersonalityType.values();
    }

    /**
     * Get all available conversation scenarios.
     */
    @GET
    @Path("/scenarios")
    public Scenario[] getScenarios() {
        return Scenario.values();
    }

    // Session management

    /**
     * Create a new conversation session with specified personality and scenario.
     */
    @POST
    @Path("/sessions")
    public Session startSession(CreateSessionRequest request) {
        Log.infof("Creating new session with userId: %s, personality: %s, scenario: %s",
                request != null ? request.userId() : "null",
                request != null ? request.personality() : "null",
                request != null ? request.scenario() : "null");

        if (request == null || request.userId() == null || request.userId().trim().isEmpty() ||
                request.personality() == null || request.scenario() == null) {
            Log.error("Session creation failed: missing required parameters");
            throw new IllegalArgumentException("User ID, personality and scenario are required");
        }

        String sessionId = UUID.randomUUID().toString();
        Session session = new Session(sessionId, request.userId().trim(), request.personality(), request.scenario());

        sessions.put(sessionId, session);

        Log.infof("Session created successfully. ID: %s, User: %s, Total sessions in map: %d",
                sessionId, request.userId(), sessions.size());
        Log.infof("Active sessions in memory store: %d", memoryStore.getActiveSessions().size());

        return session;
    }

    // Conversation endpoints

    /**
     * Send a message to the AI manager and get a response.
     */
    @POST
    @Path("/sessions/{id}/messages")
    public Message sendMessage(@PathParam("id") String sessionId, UserMessageRequest messageRequest) {
        Log.infof("Sending message to session: %s, content length: %d",
                sessionId,
                messageRequest != null && messageRequest.content() != null ? messageRequest.content().length() : 0);

        if (messageRequest == null || messageRequest.content() == null || messageRequest.content().trim().isEmpty()) {
            Log.error("Message sending failed: empty content");
            throw new IllegalArgumentException("Message content is required");
        }

        Session session = getSessionOrThrow(sessionId);
        Log.infof("Found session: %s, personality: %s, scenario: %s",
                sessionId, session.personality().name(), session.scenario().name());

        // Log user message
        session.addMessage(new Message(USER_ROLE, messageRequest.content()));
        Log.infof("Added user message to session log. Total messages in session: %d", session.messages().size());

        // Get AI response with automatic memory management via @MemoryId
        Log.infof("Calling AI assistant for session: %s, user: %s", sessionId, session.userName());
        String aiResponse = managerAssistant.chat(
                sessionId,
                messageRequest.content(),
                session.personality().getSystemPrompt(),
                session.scenario().getPrompt(),
                session.userName());

        Log.infof("AI response received. Length: %d characters", aiResponse != null ? aiResponse.length() : 0);
        Log.infof("Active sessions in memory store after AI call: %d", memoryStore.getActiveSessions().size());

        // Log AI response
        Message aiMessage = new Message(AI_ROLE, aiResponse);
        session.addMessage(aiMessage);
        Log.infof("Added AI message to session log. Total messages in session: %d", session.messages().size());

        return aiMessage;
    }

    /**
     * Get performance feedback analysis for a completed conversation.
     */
    @GET
    @Path("/sessions/{id}/feedback")
    public Feedback getFeedback(@PathParam("id") String sessionId) {
        Session session = getSessionOrThrow(sessionId);

        Log.infof("Getting feedback for session: %s, user: %s", sessionId, session.userName());
        Log.infof("Session messages count: %d", session.messages().size());
        Log.infof("Memory store messages count: %d", memoryStore.getMessageCount(sessionId));

        if (session.messages().isEmpty()) {
            return new Feedback(null, List.of(), List.of("No conversation to analyze"));
        }

        // Format conversation history for analysis with better role labeling
        String conversationHistory = session.messages().stream()
                .map(msg -> {
                    String role = "user".equals(msg.sender()) ? session.userName() : "Manager";
                    return role + ": " + msg.content();
                })
                .collect(Collectors.joining("\n"));

        Log.infof("Conversation history for analysis (length: %d chars): %s",
                conversationHistory.length(),
                conversationHistory.length() > 200 ? conversationHistory.substring(0, 200) + "..."
                        : conversationHistory);

        // Get AI-generated feedback with user context
        Log.infof("Calling FeedbackAssistant for session: %s, user: %s", sessionId, session.userName());
        String feedbackJson = feedbackAssistant.analyze(
                conversationHistory,
                session.scenario().name(),
                session.personality().name(),
                session.userName());

        Log.infof("Received feedback JSON (length: %d chars): %s",
                feedbackJson.length(),
                feedbackJson.length() > 300 ? feedbackJson.substring(0, 300) + "..." : feedbackJson);

        try {
            // Try to parse the response as-is first
            return objectMapper.readValue(feedbackJson, Feedback.class);
        } catch (Exception e) {
            Log.warnf("Failed to parse feedback JSON directly: %s", e.getMessage());
            // If direct parsing fails, try to extract JSON from the response
            try {
                String extractedJson = extractJsonFromResponse(feedbackJson);
                Log.infof("Extracted JSON: %s", extractedJson);
                return objectMapper.readValue(extractedJson, Feedback.class);
            } catch (Exception ex) {
                Log.errorf("Failed to extract and parse JSON: %s", ex.getMessage());
                // If all parsing fails, return a fallback response
                return new Feedback(
                        null,
                        List.of("Conversation completed"),
                        List.of("Unable to generate detailed feedback. Raw response: " +
                                (feedbackJson.length() > 200 ? feedbackJson.substring(0, 200) + "..." : feedbackJson)));
            }
        }
    }

    // Session management endpoints

    /**
     * Get all active sessions with their basic information.
     */
    @GET
    @Path("/sessions")
    public List<SessionInfo> getAllSessions() {
        Set<String> activeSessions = memoryStore.getActiveSessions();
        Set<String> sessionMapKeys = sessions.keySet();

        Log.infof("GET /sessions called. Sessions in map: %d, Sessions in memory store: %d",
                sessionMapKeys.size(), activeSessions.size());
        Log.infof("Session map keys: %s", sessionMapKeys);
        Log.infof("Memory store session keys: %s", activeSessions);

        // Combine sessions from both sources
        Set<String> allSessionIds = new java.util.HashSet<>(sessionMapKeys);
        allSessionIds.addAll(activeSessions);

        Log.infof("Total unique sessions to return: %d", allSessionIds.size());

        return allSessionIds.stream()
                .map(sessionId -> {
                    Session session = sessions.get(sessionId);
                    int messageCount = memoryStore.getMessageCount(sessionId);

                    Log.debugf("Processing session %s: session exists=%s, message count=%d",
                            sessionId, session != null, messageCount);

                    if (session != null) {
                        return new SessionInfo(
                                sessionId,
                                session.userName(),
                                session.personality().name(),
                                session.scenario().name(),
                                session.status(),
                                messageCount,
                                session.createdAt());
                    } else {
                        // Memory exists but session metadata is missing
                        Log.warnf("Orphaned session found in memory store: %s", sessionId);
                        return new SessionInfo(
                                sessionId,
                                "Unknown User",
                                "Unknown",
                                "Unknown",
                                "ORPHANED",
                                messageCount,
                                null);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Get detailed information about a specific session.
     */
    @GET
    @Path("/sessions/{id}")
    public SessionInfo getSession(@PathParam("id") String sessionId) {
        Session session = sessions.get(sessionId);
        int messageCount = memoryStore.getMessageCount(sessionId);

        if (session == null && messageCount == 0) {
            throw new NotFoundException("Session not found: " + sessionId);
        }

        if (session != null) {
            return new SessionInfo(
                    sessionId,
                    session.userName(),
                    session.personality().name(),
                    session.scenario().name(),
                    session.status(),
                    messageCount,
                    session.createdAt());
        } else {
            return new SessionInfo(
                    sessionId,
                    "Unknown User",
                    "Unknown",
                    "Unknown",
                    "ORPHANED",
                    messageCount,
                    null);
        }
    }

    /**
     * Delete a session and its memory.
     */
    @DELETE
    @Path("/sessions/{id}")
    public void deleteSession(@PathParam("id") String sessionId) {
        Log.infof("API request to delete session: %s", sessionId);
        // Remove from both session storage and memory store
        sessions.remove(sessionId);
        memoryStore.forceDeleteSession(sessionId);
        Log.infof("Session %s deleted successfully", sessionId);
    }

    /**
     * Clear all sessions (useful for testing/maintenance).
     */
    @DELETE
    @Path("/sessions")
    public void clearAllSessions() {
        Log.infof("API request to clear all sessions. Sessions in map: %d, Sessions in memory: %d",
                sessions.size(), memoryStore.getActiveSessions().size());
        sessions.clear();
        memoryStore.clearAllSessions();
        Log.info("All sessions cleared successfully");
    }

    /**
     * Debug endpoint to show current session state.
     */
    @GET
    @Path("/sessions/debug")
    public Map<String, Object> getSessionDebugInfo() {
        Set<String> sessionMapKeys = sessions.keySet();
        Set<String> memoryStoreKeys = memoryStore.getActiveSessions();

        Map<String, Object> debugInfo = new java.util.HashMap<>();
        debugInfo.put("sessionsInMap", sessionMapKeys.size());
        debugInfo.put("sessionMapKeys", sessionMapKeys);
        debugInfo.put("sessionsInMemoryStore", memoryStoreKeys.size());
        debugInfo.put("memoryStoreKeys", memoryStoreKeys);

        // Detailed session info
        Map<String, Object> sessionDetails = new java.util.HashMap<>();
        for (String sessionId : sessionMapKeys) {
            Session session = sessions.get(sessionId);
            if (session != null) {
                Map<String, Object> detail = new java.util.HashMap<>();
                detail.put("userName", session.userName());
                detail.put("personality", session.personality().name());
                detail.put("scenario", session.scenario().name());
                detail.put("status", session.status());
                detail.put("createdAt", session.createdAt());
                detail.put("messagesInSession", session.messages().size());
                detail.put("messagesInMemoryStore", memoryStore.getMessageCount(sessionId));
                sessionDetails.put(sessionId, detail);
            }
        }
        debugInfo.put("sessionDetails", sessionDetails);

        Log.infof("Debug info requested. Returning: %s", debugInfo);
        return debugInfo;
    }

    // Helper methods

    private Session getSessionOrThrow(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            throw new NotFoundException("Session not found: " + sessionId);
        }
        return session;
    }

    /**
     * Extract JSON object from AI response that might contain extra text.
     */
    private String extractJsonFromResponse(String response) {
        if (response == null)
            return "{}";

        // Find the first opening brace
        int startIndex = response.indexOf('{');
        if (startIndex == -1) {
            throw new IllegalArgumentException("No JSON object found in response");
        }

        // Find the matching closing brace
        int braceCount = 0;
        int endIndex = -1;

        for (int i = startIndex; i < response.length(); i++) {
            char c = response.charAt(i);
            if (c == '{') {
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    endIndex = i;
                    break;
                }
            }
        }

        if (endIndex == -1) {
            throw new IllegalArgumentException("No complete JSON object found in response");
        }

        return response.substring(startIndex, endIndex + 1);
    }

    // DTOs

    record CreateSessionRequest(String userId, PersonalityType personality, Scenario scenario) {
    }

    record UserMessageRequest(String content) {
    }

    record SessionInfo(
            String id,
            String userName,
            String personality,
            String scenario,
            String status,
            int messageCount,
            java.time.Instant createdAt) {
    }

    record Feedback(Integer overallScore, List<String> strengths, List<String> improvements) {
        // Constructor for fallback cases without score
        public Feedback(List<String> strengths, List<String> improvements) {
            this(null, strengths, improvements);
        }
    }
}