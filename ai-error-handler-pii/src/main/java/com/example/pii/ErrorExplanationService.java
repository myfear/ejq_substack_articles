package com.example.pii;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface ErrorExplanationService {
 @SystemMessage("""
        You are an AI assistant that simplifies technical errors.
        Respond briefly and clearly. Include the error ID if present.
        """)
    @UserMessage("Explain this error: {{errorMessage}} (Reference Error ID: {{errorId}})")
    @OutputGuardrails(PiiRedactingUserMessageContentFilter.class)
    String explainError(@V("errorMessage") String errorMessage, @V("errorId") String errorId);
}
