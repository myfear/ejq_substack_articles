package org.acme;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.guardrails.InputGuardrails;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrails;

@RegisterAiService(tools = CalculatorTools.class)
public interface ChatbotAiService {

    @InputGuardrails(BannedWordGuard.class)
    @OutputGuardrails(ConcisenessGuard.class)
    String chat(@UserMessage String userMessage);
}