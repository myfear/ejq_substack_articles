package org.acme;

import org.acme.tracing.LLMCallTracking;

import dev.langchain4j.data.message.UserMessage;
import io.quarkiverse.langchain4j.guardrails.InputGuardrail;
import io.quarkiverse.langchain4j.guardrails.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@LLMCallTracking
public class BannedWordGuard implements InputGuardrail {

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String text = userMessage.singleText();
        if (text.toLowerCase().contains("politics")) {
            return fatal("This topic is not allowed.");
        }
        return success();
    }
}