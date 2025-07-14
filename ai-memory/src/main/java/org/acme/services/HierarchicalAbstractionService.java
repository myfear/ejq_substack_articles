package org.acme.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.acme.aiservices.AbstractionAiService;
import org.acme.entities.MemoryCluster;
import org.acme.entities.MemoryFragment;
import org.acme.repositories.MemoryClusterRepository;
import org.acme.repositories.MemoryFragmentRepository;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service responsible for creating hierarchical abstractions of memory fragments.
 * 
 * <p>This service implements a hierarchical abstraction system that condenses related
 * memory fragments into higher-level summaries. The abstraction process helps manage
 * memory growth while preserving important semantic information.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><strong>Cluster-Based Abstraction:</strong> Creates abstractions from clustered memory groups</li>
 *   <li><strong>Hierarchical Structure:</strong> Supports multiple levels of abstraction</li>
 *   <li><strong>Importance Scoring:</strong> Calculates importance scores for abstract memories</li>
 *   <li><strong>AI-Powered Summarization:</strong> Uses LLM to generate meaningful abstractions</li>
 *   <li><strong>Embedding Integration:</strong> Adds abstractions to the embedding store for retrieval</li>
 * </ul>
 * 
 * <p>The service runs periodically to process clusters that don't already have abstractions,
 * maintaining the hierarchical memory structure and enabling efficient multi-level retrieval.</p>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@ApplicationScoped
public class HierarchicalAbstractionService {

    /**
     * Maximum allowed abstraction level to prevent runaway abstraction loops.
     */
    private static final int MAX_ABSTRACTION_LEVEL = 5;

    @Inject
    MemoryClusterRepository clusterRepository;

    @Inject
    MemoryFragmentRepository fragmentRepository;

    @Inject
    AbstractionAiService abstractionAiService;

    @Inject
    EmbeddingModel embeddingModel;
    
    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    /**
     * Creates abstractions for memory clusters that don't already have them.
     * 
     * <p>This method runs periodically to maintain the hierarchical abstraction structure.
     * The process involves:</p>
     * <ol>
     *   <li>Finding clusters that don't have existing abstractions</li>
     *   <li>Retrieving all members of each cluster</li>
     *   <li>Using AI to generate meaningful abstractions from cluster content</li>
     *   <li>Creating new MemoryFragment entities with higher abstraction levels</li>
     *   <li>Calculating importance scores based on cluster characteristics</li>
     *   <li>Adding abstractions to the embedding store for future retrieval</li>
     * </ol>
     * 
     * <p>The service respects the maximum abstraction level limit to prevent
     * infinite abstraction loops and ensures that abstractions are only created
     * for clusters with sufficient content.</p>
     * 
     * @throws RuntimeException if abstraction creation fails due to AI service or database issues
     */
    @Scheduled(every = "6h")
    @Transactional
    public void createAbstractions() {
        Log.infof("Abstraction: Starting periodic abstraction task");
        
        // 1. Find clusters that don't have abstractions yet
        List<MemoryCluster> clusters = clusterRepository.listAll();
        
        if (clusters.isEmpty()) {
            Log.infof("Abstraction: No clusters found, skipping abstraction");
            return;
        }
        
        Log.infof("Abstraction: Found %d clusters to evaluate for abstraction", clusters.size());
        
        int abstractionsCreated = 0;
        
        for (MemoryCluster cluster : clusters) {
            try {
                // Check if this cluster already has an abstraction
                List<MemoryFragment> existingAbstractions = fragmentRepository
                    .find("clusterId = ?1 AND abstractionLevel > 1", cluster.getClusterId())
                    .list();
                
                if (!existingAbstractions.isEmpty()) {
                    Log.infof("Abstraction: Cluster %s already has %d abstractions, skipping", 
                            cluster.getClusterId(), existingAbstractions.size());
                    continue;
                }
                
                // Get all members of this cluster
                List<MemoryFragment> members = fragmentRepository.findByClusterId(cluster.getClusterId());
                
                if (members.size() < 2) {
                    Log.infof("Abstraction: Cluster %s has only %d members, skipping abstraction", 
                            cluster.getClusterId(), members.size());
                    continue;
                }
                
                // Skip if any member is already at max abstraction level
                if (members.stream().anyMatch(m -> m.getAbstractionLevel() >= MAX_ABSTRACTION_LEVEL)) {
                    Log.infof("Abstraction: Cluster %s contains members at max abstraction level, skipping", 
                            cluster.getClusterId());
                    continue;
                }
                
                Log.infof("Abstraction: Creating abstraction for cluster %s with %d members", 
                        cluster.getClusterId(), members.size());
                
                // 2. Combine member texts for abstraction
                String combinedText = members.stream()
                    .map(MemoryFragment::getOriginalText)
                    .collect(Collectors.joining(" "));
                
                // 3. Generate abstraction using AI service
                String abstraction = abstractionAiService.summarize(combinedText);
                Log.infof("Abstraction: Generated abstraction (length: %d): %s", 
                        abstraction.length(), abstraction.substring(0, Math.min(100, abstraction.length())));
                
                // 4. Calculate importance score
                double importanceScore = calculateImportanceScore(members);
                
                // 5. Create new MemoryFragment for the abstraction
                MemoryFragment abstractedFragment = new MemoryFragment();
                abstractedFragment.setOriginalText(abstraction);
                abstractedFragment.setAbstractionLevel(2); // One level higher than originals
                abstractedFragment.setImportanceScore(importanceScore);
                abstractedFragment.setClusterId(cluster.getClusterId());
                abstractedFragment.setCreatedAt(LocalDateTime.now());
                abstractedFragment.setLastAccessed(LocalDateTime.now());
                abstractedFragment.setAccessCount(0);
                
                // 6. Generate embedding for the abstraction
                Embedding abstractionEmbedding = embeddingModel.embed(abstraction).content();
                abstractedFragment.setEmbedding(abstractionEmbedding.vector());
                
                // 7. Set up parent-child relationships
                for (MemoryFragment member : members) {
                    member.setParentMemory(abstractedFragment);
                }
                abstractedFragment.setChildMemories(members);
                
                // 8. Persist the abstraction
                fragmentRepository.persist(abstractedFragment);
                
                // 9. Add to embedding store
                addAbstractionToEmbeddingStore(abstractedFragment);
                
                abstractionsCreated++;
                Log.infof("Abstraction: Successfully created abstraction with ID %d for cluster %s", 
                        abstractedFragment.id, cluster.getClusterId());
                
            } catch (Exception e) {
                Log.errorf("Abstraction: Failed to create abstraction for cluster %s: %s", 
                        cluster.getClusterId(), e.getMessage());
            }
        }
        
        Log.infof("Abstraction: Abstraction task completed, created %d new abstractions", abstractionsCreated);
    }

    /**
     * Calculates the importance score for an abstraction based on its member characteristics.
     * 
     * <p>The importance score is derived from multiple factors:</p>
     * <ul>
     *   <li><strong>Cluster Size:</strong> Larger clusters (more members) get higher scores</li>
     *   <li><strong>Access Patterns:</strong> Frequently accessed members contribute to higher scores</li>
     *   <li><strong>Member Importance:</strong> Average importance of member fragments</li>
     *   <li><strong>Recency:</strong> Recently accessed members contribute to higher scores</li>
     * </ul>
     * 
     * <p>The calculated score is normalized to a range typically between 0.5 and 1.0,
     * with abstractions generally receiving higher importance scores than their
     * constituent members to reflect their summarization value.</p>
     * 
     * @param members list of MemoryFragment objects that form the cluster
     * @return double representing the calculated importance score (0.0 to 1.0)
     */
    private double calculateImportanceScore(List<MemoryFragment> members) {
        if (members.isEmpty()) {
            return 0.5; // Default importance
        }
        
        // Base score from cluster size (more members = more important)
        double sizeScore = Math.min(0.3, members.size() * 0.05); // Cap at 30%
        
        // Average importance of members
        double avgMemberImportance = members.stream()
            .mapToDouble(m -> m.getImportanceScore() != null ? m.getImportanceScore() : 0.5)
            .average()
            .orElse(0.5);
        
        // Access frequency bonus
        double avgAccessCount = members.stream()
            .mapToDouble(m -> m.getAccessCount() != null ? m.getAccessCount() : 0)
            .average()
            .orElse(0);
        double accessBonus = Math.min(0.2, avgAccessCount * 0.05); // Cap at 20%
        
        // Final importance score (abstractions are generally more important than originals)
        double finalScore = 0.6 + sizeScore + (avgMemberImportance * 0.2) + accessBonus;
        
        return Math.min(1.0, finalScore); // Cap at 1.0
    }

    /**
     * Adds an abstraction to the embedding store for future retrieval.
     * 
     * <p>This method creates a TextSegment with comprehensive metadata and adds it
     * to the embedding store, making the abstraction available for semantic search
     * and retrieval operations. The metadata includes all relevant information
     * about the abstraction's context and relationships.</p>
     * 
     * @param abstractedFragment the MemoryFragment containing the abstraction to add
     * @throws RuntimeException if adding to the embedding store fails
     */
    private void addAbstractionToEmbeddingStore(MemoryFragment abstractedFragment) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", abstractedFragment.id.toString());
        metadata.put("abstraction_level", abstractedFragment.getAbstractionLevel());
        metadata.put("importance_score", abstractedFragment.getImportanceScore());
        // Only add cluster_id if it's not null (should always be set for abstractions)
        if (abstractedFragment.getClusterId() != null) {
            metadata.put("cluster_id", abstractedFragment.getClusterId());
        }
        metadata.put("created_at", abstractedFragment.getCreatedAt().toString());
        metadata.put("last_accessed", abstractedFragment.getLastAccessed().toString());
        metadata.put("access_count", abstractedFragment.getAccessCount());
        metadata.put("type", "abstraction");
        
        TextSegment textSegment = TextSegment.from(abstractedFragment.getOriginalText(), new Metadata(metadata));
        Embedding embedding = Embedding.from(abstractedFragment.getEmbedding());
        
        embeddingStore.add(embedding, textSegment);
        
        Log.infof("Abstraction: Added abstraction to embedding store with ID %s", 
                abstractedFragment.id.toString());
    }
}
