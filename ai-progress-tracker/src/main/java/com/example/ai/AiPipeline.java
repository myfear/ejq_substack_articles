package com.example.ai;

import com.example.progress.ProgressEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AiPipeline {

    @Inject
    Assistant assistant;

    public interface ProgressCallback {
        void onProgress(ProgressEvent event);
    }

    public String processWithProgress(String question, ProgressCallback callback) {
        try {
            // Step 1: Normalize query
            callback.onProgress(new ProgressEvent("Normalize query", 20, "Query normalized"));
            String q = normalize(question);

            // Step 2: Retrieve documents
            callback.onProgress(new ProgressEvent("Retrieve documents", 40, "Documents retrieved"));
            String preview = retrievePreview(q);

            // Step 3: Build final prompt
            callback.onProgress(new ProgressEvent("Build final prompt", 60, "Prompt built"));
            String finalPrompt = buildPrompt(q, preview);

            // Step 4: Call LLM
            callback.onProgress(new ProgressEvent("Call LLM", 80, "LLM called"));
            String raw = callLlm(finalPrompt);
            String answer = compose(raw);

            // Step 5: Complete
            callback.onProgress(new ProgressEvent("Answer", 100, answer));

            return answer;
        } catch (Exception e) {
            callback.onProgress(new ProgressEvent("Error", 0, e.getMessage()));
            throw e;
        }
    }

    public String normalize(String q) {
        return q == null ? "" : q.trim().replaceAll("\\s+", " ");
    }

    public String retrievePreview(String q) {
        // Ask the model to surface only the top snippets (forces retrieval path)
        return assistant.answer("List 3 short bullets relevant to: " + q);
    }

    public String buildPrompt(String q, String preview) {
        return """
                Using the following context, answer clearly:

                Question: %s

                Context:
                %s
                """.formatted(q, preview);
    }

    public String callLlm(String finalPrompt) {
        return assistant.answer(finalPrompt);
    }

    public String compose(String raw) {
        // Could format markdown, add citations, etc.
        return raw;
    }
}