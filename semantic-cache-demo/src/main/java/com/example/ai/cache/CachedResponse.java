package com.example.ai.cache;

import java.time.Instant;

public class CachedResponse {
    private final Object response;
    private final Instant createdAt;
    private final double similarityScore;
    private final String matchedPrompt;
    private final boolean exactMatch;

    public CachedResponse(Object response, Instant createdAt,
            double similarityScore, String matchedPrompt,
            boolean exactMatch) {
        this.response = response;
        this.createdAt = createdAt;
        this.similarityScore = similarityScore;
        this.matchedPrompt = matchedPrompt;
        this.exactMatch = exactMatch;
    }

    // Getters
    public Object getResponse() {
        return response;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public String getMatchedPrompt() {
        return matchedPrompt;
    }

    public boolean isExactMatch() {
        return exactMatch;
    }
}
