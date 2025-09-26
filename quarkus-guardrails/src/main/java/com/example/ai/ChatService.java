package com.example.ai;

import com.example.guardrails.BMPromptInjectionGuardrail;
import com.example.guardrails.ComplianceGuardrail;
import com.example.guardrails.JsonFormatGuardrail;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface ChatService {
    @SystemMessage("""
            You are a helpful assistant that can answer questions and help with tasks.
            Respond in valid JSON format with the following structure:
            {"reply": "your response here"}
            """)
    @InputGuardrails({BMPromptInjectionGuardrail.class, ComplianceGuardrail.class})
    @OutputGuardrails(JsonFormatGuardrail.class)
    ChatResponse chat(String message);
}