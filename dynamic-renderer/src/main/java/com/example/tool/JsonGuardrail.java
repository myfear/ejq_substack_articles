package com.example.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrail;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class JsonGuardrail implements OutputGuardrail {

    @Inject
    ObjectMapper mapper;

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        try {
            mapper.readTree(responseFromLLM.text());
        } catch (Exception e) {
            return reprompt("Invalid JSON", e, "Make sure you return a valid JSON object");
        }
        return success();
    }

}
