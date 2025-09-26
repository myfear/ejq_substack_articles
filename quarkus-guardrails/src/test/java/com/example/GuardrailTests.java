package com.example;

import static dev.langchain4j.test.guardrail.GuardrailAssertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.guardrails.ComplianceGuardrail;
import com.example.guardrails.JsonFormatGuardrail;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.GuardrailResult.Result;

public class GuardrailTests {

    ComplianceGuardrail gr = new ComplianceGuardrail();
    JsonFormatGuardrail jfgr = new JsonFormatGuardrail();

    @Test
    void testComplianceGuardrail() {
        var userMessage = UserMessage.from("{\"message\":\"Just say hello\"}");
        var result = gr.validate(userMessage);

        assertThat(result)
                .hasResult(Result.SUCCESS);
    }

    @Test
    void testJsonFormatGuardrail() {
        var aiMessage = AiMessage.from("{\"message\":\"Just say hello\"}");
        var result = jfgr.validate(aiMessage);

        assertThat(result)
                .isSuccessful();

    }
}
