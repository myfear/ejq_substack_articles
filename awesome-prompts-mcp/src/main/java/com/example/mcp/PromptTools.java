package com.example.mcp;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.model.Prompt;
import com.example.service.PromptRepository;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class PromptTools {

    @Inject
    PromptRepository repo;

    @Tool(description = "Search prompts by query and category")
    public String search(
            @ToolArg(description = "Search text") String query,
            @ToolArg(description = "Category") String category) {
        Prompt.PromptCategory cat = null;
        if (category != null && !category.isBlank()) {
            try {
                cat = Prompt.PromptCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                return "Invalid category: " + category;
            }
        }
        List<Prompt> res = repo.search(query, cat, List.of());
        if (res.isEmpty())
            return "No prompts found.";
        return res.stream()
                .map(p -> String.format("• %s (%s)%n", p.title(), p.category()))
                .collect(Collectors.joining());
    }

    @Tool(description = "List categories with prompt counts")

    public String categories() {
        Map<Prompt.PromptCategory, Long> counts = repo.all().stream()
                .collect(Collectors.groupingBy(Prompt::category, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }
}
