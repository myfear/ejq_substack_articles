package org.acme;

import java.util.Map;
import java.util.stream.Collectors;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.quarkiverse.langchain4j.ModelName;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SmartChatService {

    // Model definitions with routing descriptions
    private static final Map<String, ModelDefinition> MODELS = Map.of(
            "default", new ModelDefinition(
                    "llama3.2",
                    "A route for general-purpose conversations, casual chat, and everyday questions"),
            "coder", new ModelDefinition(
                    "codellama",
                    "A route for answering questions about code, programming, software development, and technical tasks"),
            "summarizer", new ModelDefinition(
                    "mistral",
                    "A route for summarizing long texts, articles, documents, and content analysis"));

    // Model aliases for flexible naming
    private static final Map<String, String> ALIASES = Map.of(
            "chat", "default",
            "code", "coder",
            "codellama", "coder",
            "llama3.2", "default",
            "mistral", "summarizer",
            "summary", "summarizer");

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    ChatModel defaultModel;

    @Inject
    @ModelName("coder")
    ChatModel coderModel;

    @Inject
    @ModelName("summarizer")
    ChatModel summarizerModel;

    // Pre-computed embeddings for routing
    private Map<String, Embedding> modelEmbeddings;

    @PostConstruct
    void initialize() {
        // Pre-compute embeddings for each model's description
        modelEmbeddings = MODELS.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> embeddingModel.embed(entry.getValue().description()).content()));
    }

    /**
     * Chat with automatic model selection based on prompt content
     */
    public String smartChat(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }

        String selectedModel = selectModelForPrompt(prompt);
        return executeChat(selectedModel, prompt);
    }

    /**
     * Chat with a specific model
     */
    public String chat(String modelName, String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }

        String resolvedModel = resolveModelName(modelName);
        return executeChat(resolvedModel, prompt);
    }

    /**
     * Get detailed chat result with model selection information
     */
    public ChatResult chatWithDetails(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty");
        }

        String selectedModel = selectModelForPrompt(prompt);
        ModelDefinition modelDef = MODELS.get(selectedModel);
        String response = executeChat(selectedModel, prompt);

        return new ChatResult(prompt, selectedModel, modelDef, response);
    }

    /**
     * Get the model that would be selected for a prompt (without chatting)
     */
    public String getSelectedModel(String prompt) {
        return selectModelForPrompt(prompt);
    }

    /**
     * Semantic routing: select best model based on prompt content
     */
    private String selectModelForPrompt(String prompt) {
        Embedding promptEmbedding = embeddingModel.embed(prompt).content();

        return modelEmbeddings.entrySet().stream()
                .map(entry -> Map.entry(
                        entry.getKey(),
                        cosineSimilarity(promptEmbedding.vector(), entry.getValue().vector())))
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    String modelName = entry.getKey();
                    double score = entry.getValue();
                    System.out.printf("Prompt routed to '%s' with score: %.4f%n", modelName, score);
                    return modelName;
                })
                .orElse("default");
    }

    /**
     * Execute chat with resolved model
     */
    private String executeChat(String modelName, String prompt) {
        try {
            ChatModel model = getModelInstance(modelName);
            return model.chat(prompt);
        } catch (Exception e) {
            System.err.println("Error chatting with model " + modelName + ": " + e.getMessage());
            throw new RuntimeException("Failed to get response from model: " + modelName, e);
        }
    }

    /**
     * Get the actual model instance for a resolved model name
     */
    private ChatModel getModelInstance(String modelName) {
        return switch (modelName) {
            case "coder" -> coderModel;
            case "summarizer" -> summarizerModel;
            case "default" -> defaultModel;
            default -> defaultModel;
        };
    }

    /**
     * Resolve model name including aliases to canonical name
     */
    private String resolveModelName(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return "default";
        }

        String lowerName = modelName.toLowerCase().trim();

        // Check direct model name
        if (MODELS.containsKey(lowerName)) {
            return lowerName;
        }

        // Check aliases
        String aliasTarget = ALIASES.get(lowerName);
        if (aliasTarget != null) {
            return aliasTarget;
        }

        System.out.println("Unknown model: " + modelName + ". Using default.");
        return "default";
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Public API methods for information

    public String[] getAvailableModels() {
        return MODELS.keySet().toArray(new String[0]);
    }

    public String[] getAllSupportedNames() {
        var allNames = new java.util.HashSet<String>();
        allNames.addAll(MODELS.keySet());
        allNames.addAll(ALIASES.keySet());
        return allNames.toArray(new String[0]);
    }

    public ModelDefinition getModelInfo(String modelName) {
        String resolved = resolveModelName(modelName);
        return MODELS.get(resolved);
    }

    public boolean isValidModel(String modelName) {
        String resolved = resolveModelName(modelName);
        return MODELS.containsKey(resolved);
    }

    // Data classes

    public record ModelDefinition(String actualModelId, String description) {
    }

    public record ChatResult(
            String prompt,
            String selectedModel,
            ModelDefinition modelInfo,
            String response) {
        public String getFormattedResult() {
            return String.format(
                    "Model: %s (%s)\nDescription: %s\n\nResponse: %s",
                    selectedModel,
                    modelInfo.actualModelId(),
                    modelInfo.description(),
                    response);
        }
    }
}