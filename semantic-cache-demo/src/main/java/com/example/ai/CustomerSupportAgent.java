package com.example.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
interface CustomerSupportAgent {

    @SystemMessage("You are a helpful customer support agent.")
    String chat(@UserMessage String userMessage);

    String analyze(String content);
}