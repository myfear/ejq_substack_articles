package com.example.model;

import java.util.List;

public record Prompt(
        String id,
        String title,
        String description,
        String content,
        PromptCategory category,
        List<String> tags,
        String filePath,
        String exampleUsage) {
    public enum PromptCategory {
        CODE_REVIEW, TESTING, DOCUMENTATION, REFACTORING,
        DEBUGGING, ARCHITECTURE, SECURITY, PERFORMANCE, GENERAL
    }

    public String getResourceUri() {
        return "prompt://" + id;
    }
}