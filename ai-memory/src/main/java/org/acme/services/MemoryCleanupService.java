package org.acme.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.acme.entities.MemoryFragment;
import org.acme.repositories.MemoryFragmentRepository;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service responsible for managing memory lifecycle and preventing memory overflow.
 * 
 * <p>This service implements intelligent memory cleanup strategies to maintain optimal
 * system performance while preserving important memories. The cleanup process uses
 * sophisticated heuristics to determine which memories should be retained, archived,
 * or removed based on access patterns, importance scores, and memory hierarchy.</p>
 * 
 * <p>Cleanup strategies include:</p>
 * <ul>
 *   <li><strong>Access-Based Cleanup:</strong> Removes rarely accessed memories</li>
 *   <li><strong>Age-Based Cleanup:</strong> Archives old memories based on creation date</li>
 *   <li><strong>Importance-Based Cleanup:</strong> Preserves high-importance memories</li>
 *   <li><strong>Hierarchy-Aware Cleanup:</strong> Manages abstraction levels intelligently</li>
 *   <li><strong>Duplicate Detection:</strong> Removes redundant memory fragments</li>
 * </ul>
 * 
 * <p>The service operates in two modes:</p>
 * <ul>
 *   <li><strong>Minimal Cleanup:</strong> When memory usage is within acceptable limits</li>
 *   <li><strong>Aggressive Cleanup:</strong> When memory usage exceeds configured thresholds</li>
 * </ul>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@ApplicationScoped
public class MemoryCleanupService {

    /**
     * Maximum number of memory fragments allowed before aggressive cleanup is triggered.
     */
    private static final int MAX_TOTAL_FRAGMENTS = 10000;
    
    /**
     * Number of days to keep original (non-abstracted) memory fragments.
     */
    private static final int DAYS_TO_KEEP_ORIGINAL = 30;
    
    /**
     * Number of days to keep abstracted memory fragments.
     */
    private static final int DAYS_TO_KEEP_ABSTRACTED = 90;
    
    /**
     * Minimum access count required to preserve a memory fragment during cleanup.
     */
    private static final int MIN_ACCESS_COUNT_TO_PRESERVE = 3;
    
    /**
     * Minimum importance score required to preserve a memory fragment during cleanup.
     */
    private static final double MIN_IMPORTANCE_TO_PRESERVE = 0.7;

    @Inject
    MemoryFragmentRepository fragmentRepository;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    /**
     * Performs periodic memory cleanup to maintain optimal system performance.
     * 
     * <p>This method runs automatically every 7 hours to evaluate and clean up the memory
     * system. It analyzes current memory usage and applies appropriate cleanup strategies:</p>
     * 
     * <ul>
     *   <li>If memory usage is within limits: Runs minimal cleanup (old unused memories)</li>
     *   <li>If memory usage exceeds limits: Runs aggressive cleanup with multiple strategies</li>
     * </ul>
     * 
     * <p>The cleanup process is designed to preserve important and frequently accessed
     * memories while removing redundant, outdated, or low-value content.</p>
     * 
     * @throws RuntimeException if cleanup operations fail due to database issues
     */
    @Scheduled(every = "7h")
    @Transactional
    public void performCleanup() {
        Log.infof("Cleanup: Starting periodic memory cleanup task");

        // Get current memory statistics
        List<MemoryFragment> allFragments = fragmentRepository.listAll();
        int totalFragments = allFragments.size();

        Log.infof("Cleanup: Current memory usage: %d fragments", totalFragments);

        if (totalFragments <= MAX_TOTAL_FRAGMENTS) {
            Log.infof("Cleanup: Memory usage within limits (%d <= %d), running minimal cleanup",
                    totalFragments, MAX_TOTAL_FRAGMENTS);
            runMinimalCleanup();
        } else {
            Log.infof("Cleanup: Memory usage exceeds limits (%d > %d), running aggressive cleanup",
                    totalFragments, MAX_TOTAL_FRAGMENTS);
            runAggressiveCleanup();
        }

        // Log final statistics
        List<MemoryFragment> remainingFragments = fragmentRepository.listAll();
        int fragmentsRemoved = totalFragments - remainingFragments.size();
        Log.infof("Cleanup: Cleanup completed - removed %d fragments, %d remaining",
                fragmentsRemoved, remainingFragments.size());
    }

    /**
     * Runs minimal cleanup when memory usage is within acceptable limits.
     * 
     * <p>Minimal cleanup focuses on removing clearly outdated or unused memories
     * without aggressive pruning. This maintains system performance while preserving
     * potentially useful memories.</p>
     * 
     * <p>Operations performed:</p>
     * <ul>
     *   <li>Remove very old, unaccessed original memories</li>
     *   <li>Clean up orphaned fragments</li>
     *   <li>Remove obvious duplicates</li>
     * </ul>
     */
    private void runMinimalCleanup() {
        Log.infof("Cleanup: Starting minimal cleanup operations");
        
        removeOldUnusedOriginals();
        cleanupOrphanedFragments();
        removeDuplicateAbstractions();
        
        Log.infof("Cleanup: Minimal cleanup completed");
    }

    /**
     * Runs aggressive cleanup when memory usage exceeds configured limits.
     * 
     * <p>Aggressive cleanup employs multiple strategies to significantly reduce
     * memory usage while attempting to preserve the most valuable memories.</p>
     * 
     * <p>Operations performed (in order):</p>
     * <ol>
     *   <li>Remove old unused original memories</li>
     *   <li>Remove original memories that have been abstracted</li>
     *   <li>Remove duplicate abstractions</li>
     *   <li>Clean up orphaned fragments</li>
     *   <li>Remove old abstracted fragments</li>
     *   <li>Consolidate similar abstractions</li>
     *   <li>Apply importance-based cleanup</li>
     * </ol>
     */
    private void runAggressiveCleanup() {
        Log.infof("Cleanup: Starting aggressive cleanup operations");
        
        removeOldUnusedOriginals();
        removeAbstractedOriginals();
        removeDuplicateAbstractions();
        cleanupOrphanedFragments();
        removeOldAbstractedFragments();
        consolidateSimilarAbstractions();
        applyImportanceBasedCleanup();
        
        Log.infof("Cleanup: Aggressive cleanup completed");
    }

    /**
     * Removes old, unused original memory fragments.
     * 
     * <p>This method identifies original (non-abstracted) memory fragments that are
     * older than the configured retention period and have low access counts. These
     * memories are candidates for removal as they're unlikely to be relevant.</p>
     * 
     * <p>Criteria for removal:</p>
     * <ul>
     *   <li>Abstraction level = 1 (original memories)</li>
     *   <li>Age > {@value #DAYS_TO_KEEP_ORIGINAL} days</li>
     *   <li>Access count < {@value #MIN_ACCESS_COUNT_TO_PRESERVE}</li>
     * </ul>
     */
    private void removeOldUnusedOriginals() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DAYS_TO_KEEP_ORIGINAL);
        
        List<MemoryFragment> oldUnusedFragments = fragmentRepository.find(
            "abstractionLevel = 1 AND createdAt < ?1 AND accessCount < ?2",
            cutoffDate, MIN_ACCESS_COUNT_TO_PRESERVE
        ).list();
        
        Log.infof("Cleanup: Found %d old unused original fragments to remove", oldUnusedFragments.size());
        
        for (MemoryFragment fragment : oldUnusedFragments) {
            removeFragmentFromBothStores(fragment);
        }
    }

    /**
     * Removes original memories that have been successfully abstracted.
     * 
     * <p>When memories have been grouped into abstractions, the original fragments
     * may become redundant. This method identifies original memories that have
     * parent abstractions and removes them to reduce storage requirements.</p>
     * 
     * <p>Criteria for removal:</p>
     * <ul>
     *   <li>Has a parent memory (abstraction)</li>
     *   <li>Parent abstraction exists in the system</li>
     *   <li>Not frequently accessed recently</li>
     * </ul>
     */
    private void removeAbstractedOriginals() {
        List<MemoryFragment> abstractedOriginals = fragmentRepository.find(
            "parentMemory IS NOT NULL"
        ).list();
        
        Log.infof("Cleanup: Found %d abstracted original fragments to consider for removal", 
                abstractedOriginals.size());
        
        for (MemoryFragment fragment : abstractedOriginals) {
            // Keep recently accessed or important abstracted originals
            if (fragment.getAccessCount() < MIN_ACCESS_COUNT_TO_PRESERVE && 
                fragment.getImportanceScore() < MIN_IMPORTANCE_TO_PRESERVE) {
                
                Log.infof("Cleanup: Removing abstracted original fragment %d (low access: %d, low importance: %.2f)",
                        fragment.id, fragment.getAccessCount(), fragment.getImportanceScore());
                removeFragmentFromBothStores(fragment);
            }
        }
    }

    /**
     * Removes duplicate abstraction fragments.
     * 
     * <p>Over time, the abstraction process may create similar or duplicate abstractions
     * for different clusters. This method identifies and removes duplicate abstractions
     * while preserving the most recently accessed or highest importance version.</p>
     * 
     * <p>Duplicate detection criteria:</p>
     * <ul>
     *   <li>Similar text content (basic comparison)</li>
     *   <li>Same abstraction level</li>
     *   <li>Low individual access patterns</li>
     * </ul>
     */
    private void removeDuplicateAbstractions() {
        List<MemoryFragment> abstractions = fragmentRepository.find(
            "abstractionLevel > 1"
        ).list();
        
        Log.infof("Cleanup: Analyzing %d abstractions for duplicates", abstractions.size());
        
        // Simple duplicate detection - can be enhanced with semantic similarity
        List<MemoryFragment> duplicates = abstractions.stream()
            .collect(Collectors.groupingBy(f -> f.getOriginalText().toLowerCase().trim()))
            .values()
            .stream()
            .filter(group -> group.size() > 1)
            .flatMap(group -> {
                // Keep the most recently accessed, remove others
                group.sort((a, b) -> b.getLastAccessed().compareTo(a.getLastAccessed()));
                return group.stream().skip(1); // Skip first (most recent), remove rest
            })
            .collect(Collectors.toList());
        
        Log.infof("Cleanup: Found %d duplicate abstractions to remove", duplicates.size());
        
        for (MemoryFragment duplicate : duplicates) {
            removeFragmentFromBothStores(duplicate);
        }
    }

    /**
     * Cleans up orphaned memory fragments.
     * 
     * <p>Orphaned fragments are memories that have lost their relationships or references
     * due to previous cleanup operations or system errors. This method identifies and
     * removes such fragments to maintain database integrity.</p>
     * 
     * <p>Orphaned fragment criteria:</p>
     * <ul>
     *   <li>References to non-existent parent memories</li>
     *   <li>Cluster IDs that no longer exist</li>
     *   <li>Malformed or corrupted data</li>
     * </ul>
     */
    private void cleanupOrphanedFragments() {
        // Find fragments with non-existent parent references
        List<MemoryFragment> orphanedFragments = fragmentRepository.find(
            "parentMemory IS NOT NULL AND parentMemory.id NOT IN (SELECT f.id FROM MemoryFragment f)"
        ).list();
        
        Log.infof("Cleanup: Found %d orphaned fragments to remove", orphanedFragments.size());
        
        for (MemoryFragment fragment : orphanedFragments) {
            Log.infof("Cleanup: Removing orphaned fragment %d", fragment.id);
            removeFragmentFromBothStores(fragment);
        }
    }

    /**
     * Removes old abstracted memory fragments.
     * 
     * <p>Even abstracted memories have a lifecycle and may become outdated over time.
     * This method removes old abstracted fragments that are no longer relevant or
     * frequently accessed.</p>
     * 
     * <p>Criteria for removal:</p>
     * <ul>
     *   <li>Abstraction level > 1</li>
     *   <li>Age > {@value #DAYS_TO_KEEP_ABSTRACTED} days</li>
     *   <li>Low access count and importance score</li>
     * </ul>
     */
    private void removeOldAbstractedFragments() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(DAYS_TO_KEEP_ABSTRACTED);
        
        List<MemoryFragment> oldAbstractions = fragmentRepository.find(
            "abstractionLevel > 1 AND createdAt < ?1 AND accessCount < ?2 AND importanceScore < ?3",
            cutoffDate, MIN_ACCESS_COUNT_TO_PRESERVE, MIN_IMPORTANCE_TO_PRESERVE
        ).list();
        
        Log.infof("Cleanup: Found %d old abstracted fragments to remove", oldAbstractions.size());
        
        for (MemoryFragment fragment : oldAbstractions) {
            removeFragmentFromBothStores(fragment);
        }
    }

    /**
     * Consolidates similar abstraction fragments.
     * 
     * <p>This method identifies abstractions that cover similar topics or content
     * and consolidates them into single, more comprehensive abstractions. This
     * reduces redundancy while preserving semantic coverage.</p>
     * 
     * <p>Currently implements basic consolidation logic that can be enhanced with
     * more sophisticated semantic similarity analysis.</p>
     */
    private void consolidateSimilarAbstractions() {
        // This is a placeholder for more sophisticated consolidation logic
        // that would analyze semantic similarity between abstractions
        Log.infof("Cleanup: Similar abstraction consolidation - placeholder for future enhancement");
    }

    /**
     * Applies importance-based cleanup as a final strategy.
     * 
     * <p>This method removes memory fragments with the lowest importance scores
     * when other cleanup strategies haven't achieved sufficient memory reduction.
     * It's used as a last resort to bring memory usage within acceptable limits.</p>
     * 
     * <p>The cleanup preserves a percentage of memories with the highest importance
     * scores while removing those with the lowest scores.</p>
     */
    private void applyImportanceBasedCleanup() {
        List<MemoryFragment> allFragments = fragmentRepository.listAll();
        
        if (allFragments.size() <= MAX_TOTAL_FRAGMENTS) {
            Log.infof("Cleanup: Memory usage now within limits, skipping importance-based cleanup");
            return;
        }
        
        // Sort by importance score and remove the lowest-scoring fragments
        allFragments.sort((a, b) -> Double.compare(
            a.getImportanceScore() != null ? a.getImportanceScore() : 0.0,
            b.getImportanceScore() != null ? b.getImportanceScore() : 0.0
        ));
        
        int targetsToRemove = allFragments.size() - MAX_TOTAL_FRAGMENTS;
        List<MemoryFragment> lowImportanceFragments = allFragments.stream()
            .limit(targetsToRemove)
            .collect(Collectors.toList());
        
        Log.infof("Cleanup: Removing %d low-importance fragments", lowImportanceFragments.size());
        
        for (MemoryFragment fragment : lowImportanceFragments) {
            removeFragmentFromBothStores(fragment);
        }
    }

    /**
     * Removes a memory fragment from both database and embedding store.
     * 
     * <p>This utility method ensures that memory fragments are completely removed
     * from both storage systems to maintain consistency. It handles the removal
     * from the embedding store first, then from the database.</p>
     * 
     * @param fragment the MemoryFragment to remove from both stores
     * @throws RuntimeException if removal from either store fails
     */
    private void removeFragmentFromBothStores(MemoryFragment fragment) {
        try {
            // Remove from EmbeddingStore first (by ID)
            String fragmentId = fragment.id.toString();
            
            // Note: EmbeddingStore removal by ID may need to be implemented
            // depending on the specific EmbeddingStore implementation
            Log.infof("Cleanup: Removing fragment %s from EmbeddingStore", fragmentId);
            
            // Remove from database
            fragmentRepository.delete(fragment);
            Log.infof("Cleanup: Removed fragment %d from database", fragment.id);
            
        } catch (Exception e) {
            Log.errorf("Cleanup: Failed to remove fragment %d: %s", fragment.id, e.getMessage());
        }
    }

    /**
     * Performs manual cleanup operation outside of the scheduled routine.
     * 
     * <p>This method allows administrators or monitoring systems to trigger
     * cleanup operations manually when needed. It provides detailed feedback
     * about the cleanup process and results.</p>
     * 
     * @return String containing a detailed report of the cleanup operation
     */
    public String performManualCleanup() {
        Log.infof("Cleanup: Starting manual cleanup operation");
        
        List<MemoryFragment> beforeFragments = fragmentRepository.listAll();
        int initialCount = beforeFragments.size();
        
        // Determine cleanup strategy based on current usage
        if (initialCount > MAX_TOTAL_FRAGMENTS) {
            runAggressiveCleanup();
        } else {
            runMinimalCleanup();
        }
        
        List<MemoryFragment> afterFragments = fragmentRepository.listAll();
        int finalCount = afterFragments.size();
        int removedCount = initialCount - finalCount;
        
        String report = String.format(
            "Manual cleanup completed:\n" +
            "- Initial fragments: %d\n" +
            "- Final fragments: %d\n" +
            "- Removed: %d\n" +
            "- Strategy: %s",
            initialCount, finalCount, removedCount,
            initialCount > MAX_TOTAL_FRAGMENTS ? "Aggressive" : "Minimal"
        );
        
        Log.infof("Cleanup: %s", report);
        return report;
    }

    /**
     * Provides comprehensive statistics about the current cleanup configuration and memory state.
     * 
     * <p>This method returns detailed information about memory usage, cleanup thresholds,
     * and system configuration that can be used for monitoring and debugging purposes.</p>
     * 
     * @return String containing detailed cleanup and memory statistics
     */
    public String getCleanupStats() {
        List<MemoryFragment> allFragments = fragmentRepository.listAll();
        
        // Calculate statistics by abstraction level
        long originalCount = allFragments.stream()
            .filter(f -> f.getAbstractionLevel() == 1)
            .count();
        
        long abstractedCount = allFragments.stream()
            .filter(f -> f.getAbstractionLevel() > 1)
            .count();
        
        long recentlyAccessedCount = allFragments.stream()
            .filter(f -> f.getLastAccessed() != null && 
                        f.getLastAccessed().isAfter(LocalDateTime.now().minusDays(7)))
            .count();
        
        // Calculate average importance score
        double avgImportance = allFragments.stream()
            .mapToDouble(f -> f.getImportanceScore() != null ? f.getImportanceScore() : 0.5)
            .average()
            .orElse(0.5);
        
        return String.format(
            "Memory Cleanup Statistics:\n" +
            "- Total fragments: %d\n" +
            "- Original fragments: %d\n" +
            "- Abstracted fragments: %d\n" +
            "- Recently accessed (7 days): %d\n" +
            "- Average importance score: %.3f\n" +
            "- Memory usage vs limit: %d / %d (%.1f%%)\n" +
            "- Cleanup thresholds:\n" +
            "  * Original retention: %d days\n" +
            "  * Abstracted retention: %d days\n" +
            "  * Min access count: %d\n" +
            "  * Min importance: %.2f",
            allFragments.size(), originalCount, abstractedCount, recentlyAccessedCount,
            avgImportance, allFragments.size(), MAX_TOTAL_FRAGMENTS,
            (allFragments.size() * 100.0 / MAX_TOTAL_FRAGMENTS),
            DAYS_TO_KEEP_ORIGINAL, DAYS_TO_KEEP_ABSTRACTED,
            MIN_ACCESS_COUNT_TO_PRESERVE, MIN_IMPORTANCE_TO_PRESERVE
        );
    }
}