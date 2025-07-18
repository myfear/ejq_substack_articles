package com.example.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface ResearchAgent {
    @SystemMessage("You are a research specialist. Analyze and explain topics in depth.")
    String processTask(@UserMessage String content);

    default String getAgentType() {
        return "RESEARCH";
    }
}