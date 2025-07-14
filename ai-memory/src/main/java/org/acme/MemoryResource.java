package org.acme;

import java.time.LocalDateTime;
import java.util.List;

import org.acme.entities.MemoryCluster;
import org.acme.entities.MemoryFragment;
import org.acme.repositories.MemoryClusterRepository;
import org.acme.repositories.MemoryFragmentRepository;
import org.acme.services.MemoryCleanupService;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST endpoint for managing and monitoring the AI memory system.
 * 
 * <p>This resource provides HTTP endpoints for interacting with the memory system,
 * including memory storage, system monitoring, and administrative operations.
 * It serves as the primary interface for external clients to access memory
 * functionality and system status information.</p>
 * 
 * <p>Available endpoint categories:</p>
 * <ul>
 *   <li><strong>Memory Operations:</strong> Store new conversational content</li>
 *   <li><strong>Cluster Monitoring:</strong> View clustering status and statistics</li>
 *   <li><strong>Cleanup Management:</strong> Manual cleanup operations and statistics</li>
 *   <li><strong>System Diagnostics:</strong> Embedding store analysis and debugging</li>
 * </ul>
 * 
 * <p>The resource integrates with:</p>
 * <ul>
 *   <li>Reactive messaging pipeline for memory ingestion</li>
 *   <li>Repository layer for direct data access</li>
 *   <li>Services layer for business logic operations</li>
 *   <li>Embedding store for semantic search capabilities</li>
 * </ul>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@Path("/memory")
public class MemoryResource {

    @Inject
    @Channel("raw-conversation-in")
    Emitter<String> conversationEmitter;

    @Inject
    MemoryClusterRepository clusterRepository;

    @Inject
    MemoryCleanupService cleanupService;

    @Inject
    MemoryFragmentRepository memoryFragmentRepository;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    EmbeddingModel embeddingModel;

    /**
     * Stores new conversational content in the memory system.
     * 
     * <p>This endpoint accepts raw conversational text and initiates the memory
     * processing pipeline. The text is sent through the reactive messaging system
     * where it will be processed asynchronously through multiple stages:</p>
     * <ol>
     *   <li>Embedding generation using the configured embedding model</li>
     *   <li>Quantization for efficient storage</li>
     *   <li>Persistence to both database and embedding store</li>
     *   <li>Future clustering and abstraction processing</li>
     * </ol>
     * 
     * <p>The endpoint returns immediately after queuing the content for processing,
     * making it suitable for high-throughput conversational applications.</p>
     * 
     * @param conversationTurn the raw conversational text to store
     * @return HTTP 202 (Accepted) response indicating the content has been queued for processing
     */
    @POST
    @Path("/store")
    public Response storeMemory(String conversationTurn) {
        conversationEmitter.send(conversationTurn);
        return Response.accepted().build();
    }

    /**
     * Provides comprehensive status information about memory clusters.
     * 
     * <p>This endpoint returns detailed information about the current state of
     * the clustering system, including:</p>
     * <ul>
     *   <li>Total number of clusters in the system</li>
     *   <li>Number of mature clusters ready for abstraction</li>
     *   <li>Detailed information about each cluster</li>
     *   <li>Maturity assessment based on size and stability criteria</li>
     * </ul>
     * 
     * <p>Cluster maturity criteria:</p>
     * <ul>
     *   <li>Member count >= 2 fragments</li>
     *   <li>Last updated > 30 seconds ago (stability threshold)</li>
     * </ul>
     * 
     * <p>Useful for monitoring clustering progress, debugging cluster formation,
     * and understanding the semantic organization of memories.</p>
     * 
     * @return Plain text report containing comprehensive cluster status information
     */
    @GET
    @Path("/clusters/status")
    @Produces(MediaType.TEXT_PLAIN)
    public String getClustersStatus() {
        List<MemoryCluster> allClusters = clusterRepository.listAll();
        List<MemoryCluster> matureClusters = clusterRepository.findMatureClusters();

        StringBuilder status = new StringBuilder();
        status.append("=== CLUSTER STATUS ===\n");
        status.append("Total clusters: ").append(allClusters.size()).append("\n");
        status.append("Mature clusters: ").append(matureClusters.size()).append("\n");
        status.append("Current time: ").append(LocalDateTime.now()).append("\n");
        status.append("Maturity criteria: memberCount >= 2 AND lastUpdated < 30 seconds ago\n\n");

        if (allClusters.isEmpty()) {
            status.append("No clusters found.\n");
        } else {
            status.append("=== ALL CLUSTERS ===\n");
            for (MemoryCluster cluster : allClusters) {
                LocalDateTime thirtySecondsAgo = LocalDateTime.now().minusSeconds(30);
                boolean isMature = cluster.getMemberCount() >= 2 && cluster.getLastUpdated().isBefore(thirtySecondsAgo);
                long secondsAgo = java.time.Duration.between(cluster.getLastUpdated(), LocalDateTime.now()).toSeconds();

                status.append(String.format("Cluster ID: %s\n", cluster.getClusterId()));
                status.append(String.format("  Members: %d\n", cluster.getMemberCount()));
                status.append(String.format("  Theme: %s\n", cluster.getTheme()));
                status.append(String.format("  Last Updated: %s (%d seconds ago)\n", cluster.getLastUpdated(), secondsAgo));
                status.append(String.format("  Is Mature: %s\n", isMature ? "YES" : "NO"));
                status.append("\n");
            }
        }

        return status.toString();
    }

    /**
     * Retrieves comprehensive cleanup system statistics.
     * 
     * <p>This endpoint provides detailed information about the memory cleanup
     * system's current configuration and the state of memory fragments. The
     * statistics include:</p>
     * <ul>
     *   <li>Total memory fragment counts by type</li>
     *   <li>Recent access pattern analysis</li>
     *   <li>Average importance score distribution</li>
     *   <li>Memory usage relative to configured limits</li>
     *   <li>Cleanup threshold configuration</li>
     * </ul>
     * 
     * <p>Useful for monitoring memory system health, planning cleanup operations,
     * and understanding memory usage patterns over time.</p>
     * 
     * @return Plain text report containing detailed cleanup statistics
     */
    @GET
    @Path("/cleanup/stats")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCleanupStats() {
        return cleanupService.getCleanupStats();
    }

    /**
     * Triggers a manual memory cleanup operation.
     * 
     * <p>This endpoint allows administrators to initiate cleanup operations
     * outside of the regular scheduled cleanup cycle. The cleanup strategy
     * (minimal or aggressive) is automatically selected based on current
     * memory usage relative to configured thresholds.</p>
     * 
     * <p>Cleanup operations include:</p>
     * <ul>
     *   <li>Removal of old, unused memory fragments</li>
     *   <li>Cleanup of orphaned or corrupted data</li>
     *   <li>Duplicate detection and removal</li>
     *   <li>Importance-based pruning when necessary</li>
     * </ul>
     * 
     * <p>The response includes a detailed report of the cleanup operation
     * showing what was removed and the final system state.</p>
     * 
     * @return Plain text report describing the cleanup operation results
     */
    @POST
    @Path("/cleanup/manual")
    @Produces(MediaType.TEXT_PLAIN)
    public String performManualCleanup() {
        return cleanupService.performManualCleanup();
    }

    /**
     * Lists and analyzes stored embeddings with comprehensive metadata.
     * 
     * <p>This endpoint provides detailed information about embeddings stored
     * in both the database and the embedding store, enabling comparison and
     * analysis of the dual storage system. The report includes:</p>
     * 
     * <p><strong>Database Analysis:</strong></p>
     * <ul>
     *   <li>Memory fragment counts by abstraction level</li>
     *   <li>Access count and importance score distributions</li>
     *   <li>Cluster membership statistics</li>
     *   <li>Recent access patterns</li>
     * </ul>
     * 
     * <p><strong>Embedding Store Analysis:</strong></p>
     * <ul>
     *   <li>Total number of stored embeddings</li>
     *   <li>Metadata completeness assessment</li>
     *   <li>Type distribution (original vs. abstraction)</li>
     *   <li>Sample embedding details for debugging</li>
     * </ul>
     * 
     * <p><strong>System Comparison:</strong></p>
     * <ul>
     *   <li>Synchronization status between database and embedding store</li>
     *   <li>Consistency checks and discrepancy reporting</li>
     *   <li>Performance and storage efficiency metrics</li>
     * </ul>
     * 
     * <p>This endpoint is particularly useful for:</p>
     * <ul>
     *   <li>Debugging embedding storage issues</li>
     *   <li>Monitoring system synchronization</li>
     *   <li>Analyzing memory access patterns</li>
     *   <li>Performance optimization and troubleshooting</li>
     * </ul>
     * 
     * @return Plain text report containing comprehensive embedding analysis
     */
    @GET
    @Path("/embeddings")
    @Produces(MediaType.TEXT_PLAIN)
    public String listStoredEmbeddings() {
        StringBuilder report = new StringBuilder();
        report.append("=== EMBEDDING STORE ANALYSIS ===\n\n");
        
        // Database analysis
        List<MemoryFragment> allFragments = memoryFragmentRepository.listAll();
        report.append("=== DATABASE ANALYSIS ===\n");
        report.append("Total fragments in database: ").append(allFragments.size()).append("\n");
        
        // Group by abstraction level
        long level1Count = allFragments.stream().filter(f -> f.getAbstractionLevel() == 1).count();
        long level2PlusCount = allFragments.stream().filter(f -> f.getAbstractionLevel() > 1).count();
        
        report.append("Level 1 (original): ").append(level1Count).append("\n");
        report.append("Level 2+ (abstractions): ").append(level2PlusCount).append("\n");
        
        // Access patterns
        long recentlyAccessed = allFragments.stream()
            .filter(f -> f.getLastAccessed() != null && 
                        f.getLastAccessed().isAfter(LocalDateTime.now().minusDays(1)))
            .count();
        
        report.append("Recently accessed (24h): ").append(recentlyAccessed).append("\n");
        
        // Average scores
        double avgImportance = allFragments.stream()
            .mapToDouble(f -> f.getImportanceScore() != null ? f.getImportanceScore() : 0.5)
            .average().orElse(0.5);
        
        double avgAccessCount = allFragments.stream()
            .mapToDouble(f -> f.getAccessCount() != null ? f.getAccessCount() : 0)
            .average().orElse(0);
        
        report.append("Average importance score: ").append(String.format("%.3f", avgImportance)).append("\n");
        report.append("Average access count: ").append(String.format("%.1f", avgAccessCount)).append("\n");
        
        // Embedding Store analysis
        report.append("\n=== EMBEDDING STORE ANALYSIS ===\n");
        
        try {
            // Use a realistic query to get stored embeddings
            Embedding queryEmbedding = embeddingModel.embed("test query").content();
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(1000)  // Large number to get most/all embeddings
                .minScore(0.0)      // Include all regardless of similarity
                .build();
            
            List<EmbeddingMatch<TextSegment>> results = embeddingStore.search(searchRequest).matches();
            report.append("Total embeddings in store: ").append(results.size()).append("\n");
            
            if (results.isEmpty()) {
                report.append("No embeddings stored in EmbeddingStore.\n");
            } else {
                // Analyze metadata
                long withId = results.stream()
                    .filter(r -> r.embedded().metadata().getString("id") != null)
                    .count();
                
                long originalType = results.stream()
                    .filter(r -> "original".equals(r.embedded().metadata().getString("type")))
                    .count();
                
                long abstractionType = results.stream()
                    .filter(r -> "abstraction".equals(r.embedded().metadata().getString("type")))
                    .count();
                
                report.append("Embeddings with ID metadata: ").append(withId).append("\n");
                report.append("Original type embeddings: ").append(originalType).append("\n");
                report.append("Abstraction type embeddings: ").append(abstractionType).append("\n");
                
                // Show some example embeddings
                report.append("\n=== SAMPLE EMBEDDINGS ===\n");
                for (int i = 0; i < Math.min(5, results.size()); i++) {
                    EmbeddingMatch<TextSegment> match = results.get(i);
                    TextSegment segment = match.embedded();
                    
                    report.append("Sample ").append(i + 1).append(":\n");
                    report.append("  Similarity Score: ").append(String.format("%.4f", match.score())).append("\n");
                    report.append("  Text: ").append(truncateText(segment.text(), 60)).append("\n");
                    report.append("  ID: ").append(segment.metadata().getString("id")).append("\n");
                    report.append("  Type: ").append(segment.metadata().getString("type")).append("\n");
                    report.append("  Abstraction Level: ").append(segment.metadata().getString("abstraction_level")).append("\n");
                    
                    // Try to find corresponding database entry
                    String embeddingId = segment.metadata().getString("id");
                    if (embeddingId != null) {
                        try {
                            Long id = Long.parseLong(embeddingId);
                            MemoryFragment dbFragment = memoryFragmentRepository.findById(id);
                            if (dbFragment != null) {
                                report.append("  DB Access Count: ").append(dbFragment.getAccessCount()).append("\n");
                                report.append("  DB Importance: ").append(String.format("%.3f", 
                                    dbFragment.getImportanceScore() != null ? dbFragment.getImportanceScore() : 0.0)).append("\n");
                            } else {
                                report.append("  DB Entry: NOT FOUND\n");
                            }
                        } catch (NumberFormatException e) {
                            report.append("  DB Entry: Invalid ID format\n");
                        }
                    }
                    report.append("\n");
                }
            }
            
            // System comparison
            report.append("=== SYSTEM COMPARISON ===\n");
            report.append("Database fragments: ").append(allFragments.size()).append("\n");
            report.append("EmbeddingStore entries: ").append(results.size()).append("\n");
            
            if (allFragments.size() == results.size()) {
                report.append("Status: SYNCHRONIZED ✓\n");
            } else {
                report.append("Status: UNSYNCHRONIZED ⚠\n");
                report.append("Difference: ").append(Math.abs(allFragments.size() - results.size())).append(" entries\n");
            }
            
        } catch (Exception e) {
            report.append("Error accessing EmbeddingStore: ").append(e.getMessage()).append("\n");
        }
        
        return report.toString();
    }

    /**
     * Utility method to truncate long text for display purposes.
     * 
     * <p>This method ensures that text output in reports remains readable
     * by limiting the length of displayed content. Long text is truncated
     * and marked with an ellipsis to indicate truncation.</p>
     * 
     * @param text the text to potentially truncate
     * @param maxLength the maximum allowed length
     * @return the original text if shorter than maxLength, otherwise truncated text with ellipsis
     */
    private String truncateText(String text, int maxLength) {
        if (text == null) return "null";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
