package org.acme.tracing;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.acme.tracing.TraceData.ConversationTrace;
import org.acme.tracing.TraceData.GuardrailViolation;
import org.acme.tracing.TraceData.LLMInteraction;
import org.acme.tracing.TraceData.ToolCall;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LLMCallTracker {
    private final Map<String, ConversationTrace> activeTraces = new ConcurrentHashMap<>();

    public void startTrace(String conversationId, String initialPrompt) {
        activeTraces.computeIfAbsent(conversationId, id -> new ConversationTrace(
                id,
                LocalDateTime.now(),
                Collections.synchronizedList(new ArrayList<>()),
                Collections.synchronizedList(new ArrayList<>()),
                Collections.synchronizedList(new ArrayList<>()),
                new ConcurrentHashMap<>()));
    }

    public void recordLLMInteraction(String conversationId, LLMInteraction interaction) {
        Optional.ofNullable(activeTraces.get(conversationId))
                .ifPresent(trace -> trace.interactions().add(interaction));
    }

    public void recordToolCall(String conversationId, ToolCall toolCall) {
        Optional.ofNullable(activeTraces.get(conversationId))
                .ifPresent(trace -> trace.toolCalls().add(toolCall));
    }

    public void recordGuardrailViolation(String conversationId, GuardrailViolation violation) {
        Optional.ofNullable(activeTraces.get(conversationId))
                .ifPresent(trace -> trace.violations().add(violation));
    }

    public Optional<ConversationTrace> getTrace(String conversationId) {
        return Optional.ofNullable(activeTraces.get(conversationId));
    }
}
