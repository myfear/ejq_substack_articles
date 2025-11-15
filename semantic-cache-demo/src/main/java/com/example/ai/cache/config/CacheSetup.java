package com.example.ai.cache.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.example.ai.cache.store.PgVectorSemanticCacheStore;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.agroal.api.AgroalDataSource;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class CacheSetup {

    @Inject
    PgVectorSemanticCacheStore cacheStore;

    @Inject
    EmbeddingModel embeddingModel; // Injected from LangChain4j configuration

    @Inject
    AgroalDataSource dataSource;

    void onStart(@Observes StartupEvent event) {
        // Initialize database schema
        initializeDatabaseSchema();

        // Register embedding models
        cacheStore.registerEmbeddingModel("default", embeddingModel);
    }

    private void initializeDatabaseSchema() {
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {

            // Create vector extension
            stmt.execute("CREATE EXTENSION IF NOT EXISTS vector");
            Log.info("Created vector extension (if not exists)");

            // Create semantic_cache table
            String createTableSql = """
                    CREATE TABLE IF NOT EXISTS semantic_cache (
                        id SERIAL PRIMARY KEY,
                        prompt_hash VARCHAR(64) NOT NULL,
                        prompt TEXT NOT NULL,
                        llm_string TEXT NOT NULL,
                        embedding vector(384),
                        response_json JSONB NOT NULL,
                        ttl_seconds INTEGER,
                        created_at TIMESTAMP DEFAULT NOW(),
                        CONSTRAINT uk_prompt_llm UNIQUE (prompt_hash, llm_string)
                    )
                    """;
            stmt.execute(createTableSql);
            Log.info("Created semantic_cache table (if not exists)");

            // Create indexes
            try {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_prompt_hash ON semantic_cache(prompt_hash, llm_string)");
                Log.info("Created idx_prompt_hash index (if not exists)");
            } catch (SQLException e) {
                // Index might already exist, log and continue
                Log.debug("Index idx_prompt_hash may already exist: " + e.getMessage());
            }

            try {
                stmt.execute("""
                        CREATE INDEX IF NOT EXISTS idx_embedding_hnsw ON semantic_cache
                        USING hnsw (embedding vector_cosine_ops)
                        """);
                Log.info("Created idx_embedding_hnsw index (if not exists)");
            } catch (SQLException e) {
                // Index might already exist, log and continue
                Log.debug("Index idx_embedding_hnsw may already exist: " + e.getMessage());
            }

            // Create the is_expired function
            createIsExpiredFunction(conn);

        } catch (SQLException e) {
            Log.error("Error initializing database schema", e);
        }
    }

    private void createIsExpiredFunction(Connection conn) {
        String sql = """
                CREATE OR REPLACE FUNCTION is_expired(created TIMESTAMP, ttl INT)
                RETURNS BOOLEAN AS $$
                BEGIN
                    RETURN ttl IS NOT NULL AND created + (ttl || ' seconds')::INTERVAL < NOW();
                END;
                $$ LANGUAGE plpgsql IMMUTABLE
                """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            Log.info("Created is_expired function successfully");
        } catch (SQLException e) {
            Log.error("Error creating is_expired function", e);
        }
    }
}