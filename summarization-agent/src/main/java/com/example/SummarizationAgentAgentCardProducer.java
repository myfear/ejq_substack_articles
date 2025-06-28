package com.example;

import java.util.Collections;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.a2a.spec.PublicAgentCard;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class SummarizationAgentAgentCardProducer {

    @Produces
    @PublicAgentCard
    public AgentCard agentCard() {
        Log.info("agentCard() called");
        return new AgentCard.Builder()
                .name("Summarization Agent")
                .description("An agent that summarizes text.")
                .defaultInputModes(Collections.singletonList("text"))
                .defaultOutputModes(Collections.singletonList("text"))
                .url("http://localhost:8080")
                .version("1.0.0")
                .capabilities(new AgentCapabilities.Builder()
                        .streaming(false)
                        .pushNotifications(false)
                        .stateTransitionHistory(false)
                        .build())
                .skills(Collections.singletonList(new AgentSkill.Builder()
                        .id("summarize_text")
                        .name("Summarize Text")
                        .description("Summarizes the provided text.")
                        .tags(Collections.singletonList("text_summarization"))
                        .build()))
                .build();
    }
}