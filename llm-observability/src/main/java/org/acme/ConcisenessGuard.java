package org.acme;

import dev.langchain4j.data.message.AiMessage;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrail;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConcisenessGuard implements OutputGuardrail {

    private static final int MAX_LENGTH = 1500;

    @Override
    public OutputGuardrailResult validate(AiMessage aiMessage) {
        String text = aiMessage.text();
        // Allow empty content (e.g., when AI is making tool calls)
        if (text == null || text.isBlank()) {
            return success();
        }

        if (text.length() > MAX_LENGTH) {
            return reprompt("Response is too long.", "Please be more concise.");
        }
        return success();
    }
}