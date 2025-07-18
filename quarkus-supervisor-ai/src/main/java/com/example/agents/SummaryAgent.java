package com.example.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface SummaryAgent {
    @SystemMessage("You are a summarization expert. Your job is to create concise, accurate summaries of complex information. Focus on key points and clarity.")
    String processTask(@UserMessage String content);

    default String getAgentType() {
        return "SUMMARY";
    }
}
