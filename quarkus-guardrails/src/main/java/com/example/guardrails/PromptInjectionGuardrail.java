package com.example.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PromptInjectionGuardrail implements InputGuardrail {

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        Log.info("PromptInjectionGuardrail called");
        String text = userMessage.singleText().toLowerCase();

        if (text.contains("ignore previous instructions")) {
            return failure("Possible prompt injection detected");
        }

        return success();
    }
}