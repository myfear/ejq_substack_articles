package org.acme.tracing;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.acme.tracing.TraceData.GuardrailViolation;
import org.acme.tracing.TraceData.LLMInteraction;
import org.acme.tracing.TraceData.ToolCall;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.agent.tool.Tool;
import io.quarkiverse.langchain4j.guardrails.GuardrailResult;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrailResult;
import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.POST;

@LLMCallTracking
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 1)
public class LLMCallInterceptor {

    @Inject
    LLMCallTracker tracker;

    @Inject
    RequestCorrelation correlation;

    @Inject
    ObjectMapper mapper; // For serializing tool parameters

    @AroundInvoke
    public Object track(InvocationContext context) throws Exception {
        // Check if this is the entry point (the JAX-RS method)
        if (context.getMethod().isAnnotationPresent(POST.class)) {
            String conversationId = UUID.randomUUID().toString();
            Log.info("CONVERSATION ID: " + conversationId);
            correlation.setConversationId(conversationId);
            tracker.startTrace(conversationId, (String) context.getParameters()[0]);
        }

        String conversationId = correlation.getConversationId();
        if (conversationId == null) {
            // Not part of a tracked conversation, proceed without tracking
            return context.proceed();
        }

        Instant start = Instant.now();
        Object result = null;
        try {
            result = context.proceed();
            return result;
        } finally {
            Instant end = Instant.now();
            Duration duration = Duration.between(start, end);

            // Differentiate based on the type of method intercepted
            if (context.getMethod().isAnnotationPresent(Tool.class)) {
                handleToolCall(context, conversationId, result, duration);
            } else if (result instanceof GuardrailResult) {
                handleGuardrail(context, conversationId, (GuardrailResult) result);
            } else if (context.getMethod().isAnnotationPresent(POST.class)) {
                handleLLMInteraction(context, conversationId, (String) result, duration);
            }
        }
    }

    private void handleLLMInteraction(InvocationContext context, String conversationId, String response,
            Duration duration) {
        LLMInteraction interaction = new LLMInteraction(
                (String) context.getParameters()[0],
                response,
                "ollama:llama3",
                null, null,
                duration);
        tracker.recordLLMInteraction(conversationId, interaction);
    }

    private void handleToolCall(InvocationContext context, String conversationId, Object result, Duration duration) {
        String paramsJson;
        try {
            paramsJson = mapper.writeValueAsString(context.getParameters());
        } catch (Exception e) {
            paramsJson = "Error serializing params: " + e.getMessage();
        }

        ToolCall toolCall = new ToolCall(
                context.getMethod().getName(),
                paramsJson,
                String.valueOf(result),
                duration);
        tracker.recordToolCall(conversationId, toolCall);
    }

    private void handleGuardrail(InvocationContext context, String conversationId, GuardrailResult result) {
        if (!result.isSuccess()) {
            String reprompt = null;
            if (result instanceof OutputGuardrailResult) {
                reprompt = "Reprompt triggered"; // Simple fallback
            }

            GuardrailViolation violation = new GuardrailViolation(
                    context.getTarget().getClass().getSimpleName(),
                    "Guardrail violation detected", // Simple fallback message
                    reprompt);
            tracker.recordGuardrailViolation(conversationId, violation);
        }
    }
}