package org.acme.services;

import java.util.HashMap;
import java.util.Map;

import org.acme.entities.MemoryFragment;
import org.acme.repositories.MemoryFragmentRepository;
import org.acme.util.ScalarQuantizer;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.quarkus.logging.Log;
import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service responsible for processing incoming conversational text through a
 * multi-stage pipeline.
 * 
 * <p>
 * This service implements a reactive messaging pipeline that transforms raw
 * conversational text
 * into structured, searchable memory fragments. The pipeline consists of three
 * main stages:
 * </p>
 * 
 * <ol>
 * <li><strong>Embedding Stage:</strong> Converts text to high-dimensional
 * vectors using an embedding model</li>
 * <li><strong>Quantization Stage:</strong> Compresses embeddings for efficient
 * storage</li>
 * <li><strong>Persistence Stage:</strong> Stores processed memories in both
 * database and embedding store</li>
 * </ol>
 * 
 * <p>
 * The pipeline is designed to handle conversational text asynchronously,
 * providing scalability
 * and fault tolerance through Quarkus reactive messaging. Each stage can be
 * independently scaled
 * and configured based on performance requirements.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Asynchronous processing with reactive messaging</li>
 * <li>Automatic embedding generation for semantic search</li>
 * <li>Embedding quantization for storage efficiency</li>
 * <li>Dual persistence (database + embedding store)</li>
 * <li>Comprehensive metadata tracking</li>
 * </ul>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@ApplicationScoped
public class MemoryProcessingPipeline {

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    ScalarQuantizer quantizer;

    @Inject
    MemoryFragmentRepository memoryFragmentRepository;

    /**
     * Stage 1: Converts raw text into embedding vectors.
     * 
     * <p>
     * This is the first stage of the memory processing pipeline. It receives raw
     * conversational
     * text and generates high-dimensional embedding vectors using the configured
     * embedding model.
     * The embeddings capture the semantic meaning of the text for later similarity
     * searches.
     * </p>
     * 
     * <p>
     * The method processes text synchronously (blocking) to ensure proper ordering
     * and
     * resource management when dealing with the embedding model.
     * </p>
     * 
     * @param text the raw conversational text to process
     * @return IngestionPayload containing the original text and its embedding
     *         vector
     * @throws RuntimeException if embedding generation fails
     */
    @Incoming("raw-conversation-in")
    @Outgoing("embedding-out")
    @Blocking
    public IngestionPayload embedText(String text) {
        Log.infof("Stage 1: Embedding text (length: %d chars): %s", text.length(),
                text.substring(0, Math.min(100, text.length())) + (text.length() > 100 ? "..." : ""));
        Embedding embedding = embeddingModel.embed(text).content();
        Log.infof("Stage 1: Generated embedding with %d dimensions", embedding.vector().length);
        return new IngestionPayload(text, embedding);
    }

    /**
     * Stage 2: Quantizes embeddings for efficient storage.
     * 
     * <p>
     * This stage takes the full-precision embeddings from Stage 1 and applies
     * scalar quantization
     * to reduce storage requirements. The quantized embeddings are suitable for
     * archival storage
     * while the original embeddings are preserved for high-precision similarity
     * searches.
     * </p>
     * 
     * <p>
     * The quantization process balances storage efficiency with retrieval accuracy,
     * allowing
     * the system to maintain large numbers of memory fragments without excessive
     * storage costs.
     * </p>
     * 
     * @param payload the ingestion payload containing text and embedding from Stage
     *                1
     * @return IngestionPayload with added quantized embedding data
     * @throws RuntimeException if quantization fails
     */
    @Incoming("embedding-out")
    @Outgoing("quantized-out")
    public IngestionPayload quantizeEmbedding(IngestionPayload payload) {
        Log.infof("Stage 2: Quantizing embedding for text: %s",
                payload.originalText().substring(0, Math.min(50, payload.originalText().length())) + "...");

        // Convert float[] to byte[] using scalar quantization
        byte quantizedEmbedding = quantizer.quantize(payload.embedding());

        Log.infof("Stage 2: Quantized embedding from %d floats to %d bytes",
                payload.embedding().vector().length, 1);

        return new IngestionPayload(
                payload.originalText(),
                payload.embedding(),
                quantizedEmbedding,
                payload.abstractionLevel(),
                payload.importanceScore(),
                payload.clusterId(),
                payload.createdAt(),
                payload.lastAccessed(),
                payload.accessCount());
    }

    /**
     * Stage 3: Persists processed memories to both database and embedding store.
     * 
     * <p>
     * This final stage of the pipeline takes the fully processed memory data and
     * persists it
     * to both storage systems:
     * </p>
     * <ul>
     * <li><strong>Database:</strong> Stores the MemoryFragment entity with all
     * metadata</li>
     * <li><strong>Embedding Store:</strong> Stores the embedding vector for
     * semantic search</li>
     * </ul>
     * 
     * <p>
     * The method runs in a transaction to ensure data consistency and includes
     * comprehensive
     * metadata that enables sophisticated retrieval and ranking operations.
     * </p>
     * 
     * @param payload the complete ingestion payload with all processed data
     * @throws RuntimeException if persistence fails
     */
    @Incoming("quantized-out")
    @Blocking
    @Transactional
    public void persistToStore(IngestionPayload payload) {
        Log.infof("Stage 3: Persisting memory to stores for text: %s",
                payload.originalText().substring(0, Math.min(50, payload.originalText().length())) + "...");

        // 1. Create and persist MemoryFragment entity
        MemoryFragment fragment = new MemoryFragment();
        fragment.setOriginalText(payload.originalText());
        fragment.setEmbedding(payload.embedding().vector());
        fragment.setQuantizedEmbedding(payload.quantizedEmbedding());
        fragment.setAbstractionLevel(payload.abstractionLevel());
        fragment.setImportanceScore(payload.importanceScore());
        fragment.setClusterId(payload.clusterId());
        fragment.setCreatedAt(payload.createdAt());
        fragment.setLastAccessed(payload.lastAccessed());
        fragment.setAccessCount(payload.accessCount());

        memoryFragmentRepository.persist(fragment);
        Log.infof("Stage 3: Persisted MemoryFragment with ID: %d", fragment.id);

        // 2. Create metadata for EmbeddingStore
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", fragment.id.toString());
        metadata.put("abstraction_level", fragment.getAbstractionLevel());
        metadata.put("importance_score", fragment.getImportanceScore());
        // Only add cluster_id if it's not null (fragments start unclustered)
        if (fragment.getClusterId() != null) {
            metadata.put("cluster_id", fragment.getClusterId());
        }
        metadata.put("created_at", fragment.getCreatedAt().toString());
        metadata.put("last_accessed", fragment.getLastAccessed().toString());
        metadata.put("access_count", fragment.getAccessCount());
        metadata.put("type", "original");

        // 3. Create TextSegment and add to EmbeddingStore
        TextSegment textSegment = TextSegment.from(payload.originalText(), new Metadata(metadata));
        embeddingStore.add(payload.embedding(), textSegment);

        Log.infof("Stage 3: Added to EmbeddingStore with metadata: abstraction_level=%d, importance_score=%.2f",
                fragment.getAbstractionLevel(), fragment.getImportanceScore());

        Log.infof("Stage 3: Memory processing pipeline completed successfully for fragment ID: %d", fragment.id);
    }

    /**
     * Data transfer object for the memory processing pipeline.
     * 
     * <p>
     * This record carries data through the three stages of the pipeline,
     * accumulating
     * processed information at each stage. It supports both minimal construction
     * for
     * Stage 1 and full construction for final persistence.
     * </p>
     * 
     * <p>
     * The record includes all necessary fields for memory fragment creation and
     * provides convenient constructors for different stages of the pipeline.
     * </p>
     * 
     * @param originalText       the original conversational text
     * @param embedding          the high-dimensional embedding vector
     * @param quantizedEmbedding the compressed embedding for storage
     * @param abstractionLevel   the hierarchical level of this memory (1 for
     *                           original)
     * @param importanceScore    the calculated importance score (0.0 to 1.0)
     * @param clusterId          the ID of the cluster this memory belongs to (null
     *                           if unclustered)
     * @param createdAt          timestamp when the memory was created
     * @param lastAccessed       timestamp when the memory was last accessed
     * @param accessCount        number of times this memory has been accessed
     */
    public static record IngestionPayload(
            String originalText,
            Embedding embedding,
            byte quantizedEmbedding,
            Integer abstractionLevel,
            Double importanceScore,
            String clusterId,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime lastAccessed,
            Integer accessCount) {

        /**
         * Constructor for initial payload creation (before quantization).
         * 
         * <p>
         * This constructor is used by Stage 1 to create the initial payload with
         * just the original text and embedding. Default values are provided for
         * all other fields.
         * </p>
         * 
         * @param originalText the original conversational text
         * @param embedding    the generated embedding vector
         */
        public IngestionPayload(String originalText, Embedding embedding) {
            this(originalText, embedding, (byte) 0, 1, 0.5, null,
                    java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), 0);
        }
    }
}
