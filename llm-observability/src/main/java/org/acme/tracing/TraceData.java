package org.acme.tracing;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TraceData {

        public record ConversationTrace(
                        String conversationId,
                        LocalDateTime startTime,
                        List<LLMInteraction> interactions,
                        List<ToolCall> toolCalls,
                        List<GuardrailViolation> violations,
                        Map<String, Object> metadata) {
        }

        public record LLMInteraction(
                        String prompt,
                        String response,
                        String model,
                        Integer inputTokenCount,
                        Integer outputTokenCount,
                        Duration duration) {
        }

        public record ToolCall(
                        String toolName,
                        String params,
                        String result,
                        Duration duration) {
        }

        public record GuardrailViolation(
                        String guardrail,
                        String violation,
                        String reprompt) {
        }
}