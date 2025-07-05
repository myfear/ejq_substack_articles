package org.example;

import java.util.Map;

import org.example.model.StepResult;

import dev.langchain4j.model.chat.ChatModel;

public abstract class ReasoningStep {
    protected final String name;
    protected final String systemPrompt;

    public ReasoningStep(String name, String systemPrompt) {
        this.name = name;
        this.systemPrompt = systemPrompt;
    }

    public abstract StepResult execute(String input, Map<String, Object> context, ChatModel model);

    public String getName() {
        return name;
    }

    protected String generatePrompt(String input, Map<String, Object> context) {
        return input; // Can be overridden to build custom prompts using context
    }
}

