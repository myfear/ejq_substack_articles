package dev.demo.toml.service;

import dev.demo.toml.config.AiConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PromptComposer {

    public String buildSystemPrompt(AiConfiguration cfg) {
        StringBuilder sb = new StringBuilder();
        sb.append(cfg.getPrompts().getSystem().trim()).append("\n\n");

        if (cfg.getPrompts().getExamples() != null && !cfg.getPrompts().getExamples().isEmpty()) {
            sb.append("Examples:\n");
            for (AiConfiguration.PromptExample ex : cfg.getPrompts().getExamples()) {
                sb.append("User: ").append(ex.getUser()).append("\n");
                sb.append("Assistant: ").append(ex.getAssistant()).append("\n\n");
            }
        }

        sb.append("Constraints:\n");
        sb.append("- Keep responses within ").append(cfg.getFeatures().getLimits().getMaxContextLength())
                .append(" chars of context budget.\n");
        sb.append("- Prefer short paragraphs.\n");

        return sb.toString();
    }
}