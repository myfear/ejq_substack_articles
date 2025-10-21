package com.example.mcp;

import java.util.List;

import com.example.model.Prompt;
import com.example.service.PromptRepository;

import io.quarkiverse.mcp.server.RequestUri;
import io.quarkiverse.mcp.server.Resource;
import io.quarkiverse.mcp.server.ResourceTemplate;
import io.quarkiverse.mcp.server.TextResourceContents;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PromptResources {

    @Inject
    PromptRepository repo;

    @Resource(uri = "prompt://list", description = "List all available prompts")
    public TextResourceContents listAll(RequestUri uri) {
        List<Prompt> prompts = repo.all();
        StringBuilder out = new StringBuilder("# Available Prompts\n\n");
        prompts.forEach(
                p -> out.append(String.format("- **%s** (%s): %s%n", p.title(), p.category(), p.description())));
        return TextResourceContents.create(uri.value(), out.toString());
    }

    @ResourceTemplate(uriTemplate = "prompt://{id}", description = "Fetch a specific prompt")
    public TextResourceContents get(String id, RequestUri uri) {
        Prompt p = repo.byId(id);
        return p == null
                ? TextResourceContents.create(uri.value(), "Prompt not found: " + id)
                : TextResourceContents.create(uri.value(), p.content());
    }
}