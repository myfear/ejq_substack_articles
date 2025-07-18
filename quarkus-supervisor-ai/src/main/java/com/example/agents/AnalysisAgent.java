package com.example.agents;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface AnalysisAgent {
    @SystemMessage("You are a data analysis expert. Your job is to analyze data, identify patterns, and provide insights. Focus on statistical analysis and trend identification.")
    String processTask(@UserMessage String content);

    default String getAgentType() {
        return "ANALYSIS";
    }
}