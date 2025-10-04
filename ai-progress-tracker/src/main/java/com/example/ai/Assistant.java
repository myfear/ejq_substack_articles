package com.example.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface Assistant {

    @SystemMessage("""
            You are a helpful assistant. Use retrieved context when available.
            Keep answers concise and cite short bullet points from context when relevant.""")

    String answer(@UserMessage String question);
}
