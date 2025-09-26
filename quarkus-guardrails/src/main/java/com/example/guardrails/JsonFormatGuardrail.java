package com.example.guardrails;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JsonFormatGuardrail implements OutputGuardrail {

    @Override
    public OutputGuardrailResult validate(AiMessage aiMessage) {
        Log.info("JsonFormatGuardrail called");
        String output = aiMessage.text();
        if (!output.trim().startsWith("{")) {
            return failure("Response not in JSON format");
        }
        return success();
    }
}