package com.example.ai.cache.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import org.apache.commons.codec.digest.DigestUtils;

import com.example.ai.cache.CacheStatistics;
import com.example.ai.cache.CachedResponse;
import com.example.ai.cache.SemanticCacheConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.agroal.api.AgroalDataSource;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PgVectorSemanticCacheStore implements SemanticCacheStore {

    @Inject
    AgroalDataSource dataSource;

    @Inject
    ObjectMapper objectMapper;

    private final CacheStatistics statistics = new CacheStatistics();

    // Map to hold multiple embedding models
    private final java.util.Map<String, EmbeddingModel> embeddingModels = new java.util.concurrent.ConcurrentHashMap<>();

    public void registerEmbeddingModel(String name, EmbeddingModel model) {
        embeddingModels.put(name, model);
    }

    @Override
    public Optional<CachedResponse> lookup(
            String prompt,
            String llmString,
            SemanticCacheConfig config) {
        if (!config.isEnabled()) {
            return Optional.empty();
        }

        long startTime = System.currentTimeMillis();

        try {
            // Try exact match first for HYBRID strategy
            if (config.getStrategy() == SemanticCacheConfig.CacheStrategy.HYBRID ||
                    config.getStrategy() == SemanticCacheConfig.CacheStrategy.EXACT_MATCH_ONLY) {

                Optional<CachedResponse> exactMatch = lookupExact(prompt, llmString, config);
                if (exactMatch.isPresent()) {
                    statistics.recordHit(true);
                    statistics.recordLookupTime(System.currentTimeMillis() - startTime);
                    return exactMatch;
                }

                if (config.getStrategy() == SemanticCacheConfig.CacheStrategy.EXACT_MATCH_ONLY) {
                    statistics.recordMiss();
                    statistics.recordLookupTime(System.currentTimeMillis() - startTime);
                    return Optional.empty();
                }
            }

            // Semantic search
            Optional<CachedResponse> semanticMatch = lookupSemantic(prompt, llmString, config);
            if (semanticMatch.isPresent()) {
                statistics.recordHit(false);
                statistics.recordLookupTime(System.currentTimeMillis() - startTime);
                return semanticMatch;
            }

            statistics.recordMiss();
            statistics.recordLookupTime(System.currentTimeMillis() - startTime);
            return Optional.empty();

        } catch (Exception e) {
            Log.error("Error looking up cache entry", e);
            statistics.recordMiss();
            statistics.recordLookupTime(System.currentTimeMillis() - startTime);
            return Optional.empty();
        }
    }

    private Optional<CachedResponse> lookupExact(
            String prompt,
            String llmString,
            SemanticCacheConfig config) throws SQLException {
        String promptHash = computeHash(prompt, llmString);

        String sql = """
                SELECT response_json, created_at, prompt
                FROM semantic_cache
                WHERE prompt_hash = ?
                  AND llm_string = ?
                  AND is_expired(created_at, ttl_seconds) = false
                LIMIT 1
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, promptHash);
            stmt.setString(2, llmString);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                try {
                    Object response = deserializeResponse(rs.getString("response_json"));
                    return Optional.of(new CachedResponse(
                            response,
                            rs.getTimestamp("created_at").toInstant(),
                            1.0, // Perfect match
                            rs.getString("prompt"),
                            true));
                } catch (Exception e) {
                    Log.error("Error deserializing response", e);
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    private Optional<CachedResponse> lookupSemantic(
            String prompt,
            String llmString,
            SemanticCacheConfig config) throws SQLException {
        EmbeddingModel embeddingModel = embeddingModels.get(config.getEmbeddingModelName());
        if (embeddingModel == null) {
            Log.warn("Embedding model not found: " + config.getEmbeddingModelName());
            return Optional.empty();
        }

        // Generate embedding for the prompt
        Embedding embedding = embeddingModel.embed(prompt).content();
        float[] promptEmbedding = embedding.vector();

        // Use cosine similarity (1 - cosine distance)
        // Optimized: use CTE to bind vector once and reference it multiple times
        String sql = """
                WITH query_vector AS (
                    SELECT ?::vector AS vec
                )
                SELECT
                    response_json,
                    created_at,
                    prompt,
                    1 - (embedding <=> (SELECT vec FROM query_vector)) as similarity
                FROM semantic_cache, query_vector
                WHERE llm_string = ?
                  AND 1 - (embedding <=> query_vector.vec) >= ?
                  AND is_expired(created_at, ttl_seconds) = false
                ORDER BY embedding <=> query_vector.vec
                LIMIT 1
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            String vectorStr = vectorToString(promptEmbedding);
            stmt.setString(1, vectorStr); // Bind vector once
            stmt.setString(2, llmString);
            stmt.setDouble(3, config.getSimilarityThreshold());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                try {
                    Object response = deserializeResponse(rs.getString("response_json"));
                    return Optional.of(new CachedResponse(
                            response,
                            rs.getTimestamp("created_at").toInstant(),
                            rs.getDouble("similarity"),
                            rs.getString("prompt"),
                            false));
                } catch (Exception e) {
                    Log.error("Error deserializing response", e);
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void store(
            String prompt,
            String llmString,
            Object response,
            SemanticCacheConfig config) {
        if (!config.isEnabled()) {
            return;
        }

        long startTime = System.currentTimeMillis();

        try {
            EmbeddingModel embeddingModel = embeddingModels.get(config.getEmbeddingModelName());
            if (embeddingModel == null) {
                Log.warn("Embedding model not found: " + config.getEmbeddingModelName());
                return;
            }

            float[] embedding = embeddingModel.embed(prompt).content().vector();
            String promptHash = computeHash(prompt, llmString);
            String responseJson = serializeResponse(response);

            String sql = """
                    INSERT INTO semantic_cache
                        (prompt_hash, prompt, llm_string, embedding, response_json, ttl_seconds, created_at)
                    VALUES (?, ?, ?, ?::vector, ?::jsonb, ?, NOW())
                    ON CONFLICT (prompt_hash, llm_string)
                    DO UPDATE SET
                        response_json = EXCLUDED.response_json,
                        created_at = NOW()
                    """;

            try (Connection conn = dataSource.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, promptHash);
                stmt.setString(2, prompt);
                stmt.setString(3, llmString);
                stmt.setString(4, vectorToString(embedding));
                stmt.setString(5, responseJson);
                stmt.setInt(6, (int) config.getTtl().getSeconds());

                stmt.executeUpdate();
                statistics.incrementEntryCount();
            }

            statistics.recordStoreTime(System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            Log.error("Error storing cache entry", e);
            statistics.recordStoreTime(System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void clear() {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE semantic_cache");
        } catch (SQLException e) {
            Log.error("Error clearing cache", e);
        }
    }

    @Override
    public void evictExpired() {
        String sql = "DELETE FROM semantic_cache WHERE is_expired(created_at, ttl_seconds) = true";
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            int deleted = stmt.executeUpdate(sql);
            Log.info("Evicted " + deleted + " expired cache entries");
        } catch (SQLException e) {
            Log.error("Error evicting expired entries", e);
        }
    }

    @Override
    public CacheStatistics getStatistics() {
        // Update entry count before returning
        updateEntryCount();
        return statistics;
    }

    public long getEntryCount() {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM semantic_cache WHERE is_expired(created_at, ttl_seconds) = false");
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            Log.error("Error getting entry count", e);
        }
        return 0;
    }

    private void updateEntryCount() {
        long count = getEntryCount();
        statistics.updateEntryCount(count);
    }

    // Helper methods
    private String computeHash(String prompt, String llmString) {
        return DigestUtils.sha256Hex(prompt + "|" + llmString);
    }

    private String vectorToString(float[] vector) {
        if (vector == null || vector.length == 0) {
            return "[]";
        }

        // Pre-size StringBuilder: "[", "]", and commas = 2 + (length-1)
        // Average float string length ~8 chars, but we'll be conservative
        StringBuilder sb = new StringBuilder(vector.length * 10);
        sb.append('[');
        sb.append(vector[0]);
        for (int i = 1; i < vector.length; i++) {
            sb.append(',');
            sb.append(vector[i]);
        }
        sb.append(']');
        return sb.toString();
    }

    private String serializeResponse(Object response) throws Exception {
        return objectMapper.writeValueAsString(response);
    }

    private Object deserializeResponse(String json) throws Exception {
        // You may need to handle type information for proper deserialization
        return objectMapper.readValue(json, Object.class);
    }
}