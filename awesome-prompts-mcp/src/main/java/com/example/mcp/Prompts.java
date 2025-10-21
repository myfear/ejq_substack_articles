package com.example.mcp;

import com.example.service.PromptRepository;

import io.quarkiverse.mcp.server.Prompt;
import io.quarkiverse.mcp.server.PromptArg;
import io.quarkiverse.mcp.server.PromptMessage;
import io.quarkiverse.mcp.server.TextContent;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class Prompts {

    @Inject
    PromptRepository repo;

    @Prompt(description = "Apply a stored prompt with optional context")

    public PromptMessage apply(
            @PromptArg(description = "Prompt ID") String promptId,
            @PromptArg(description = "Context text") String context) {
        com.example.model.Prompt p = repo.byId(promptId);
        if (p == null)
            return PromptMessage.withUserRole(new TextContent("Prompt not found: " + promptId));

        String full = p.content();
        if (!context.isBlank())
            full += "\n\n---\n**Context:**\n```\n" + context + "\n```";
        return PromptMessage.withUserRole(new TextContent(full));
    }
}