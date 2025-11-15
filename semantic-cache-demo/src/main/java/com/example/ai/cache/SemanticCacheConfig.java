package com.example.ai.cache;

import java.time.Duration;

public class SemanticCacheConfig {
    private final boolean enabled;
    private final double similarityThreshold;
    private final Duration ttl;
    private final String embeddingModelName;
    private final CacheStrategy strategy;

    private SemanticCacheConfig(Builder builder) {
        this.enabled = builder.enabled;
        this.similarityThreshold = builder.similarityThreshold;
        this.ttl = builder.ttl;
        this.embeddingModelName = builder.embeddingModelName;
        this.strategy = builder.strategy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public Duration getTtl() {
        return ttl;
    }

    public String getEmbeddingModelName() {
        return embeddingModelName;
    }

    public CacheStrategy getStrategy() {
        return strategy;
    }

    public static class Builder {
        private boolean enabled = true;
        private double similarityThreshold = 0.85;
        private Duration ttl = Duration.ofHours(1);
        private String embeddingModelName = "default";
        private CacheStrategy strategy = CacheStrategy.SEMANTIC_ONLY;

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder similarityThreshold(double threshold) {
            this.similarityThreshold = threshold;
            return this;
        }

        public Builder ttl(Duration ttl) {
            this.ttl = ttl;
            return this;
        }

        public Builder embeddingModelName(String name) {
            this.embeddingModelName = name;
            return this;
        }

        public Builder strategy(CacheStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public SemanticCacheConfig build() {
            return new SemanticCacheConfig(this);
        }
    }

    public enum CacheStrategy {
        EXACT_MATCH_ONLY, // Fast hash-based lookup only
        SEMANTIC_ONLY, // Vector similarity only
        HYBRID // Try exact match first, then semantic
    }
}