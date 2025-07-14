package org.acme.repositories;

import java.util.List;

import org.acme.entities.MemoryFragment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for managing memory fragment data access operations.
 * 
 * <p>This repository provides specialized query methods for retrieving memory fragments
 * based on various criteria relevant to the AI memory system. It extends Quarkus
 * Panache repository functionality with custom queries optimized for memory operations.</p>
 * 
 * <p>Key query categories:</p>
 * <ul>
 *   <li><strong>Clustering Queries:</strong> Find fragments by cluster membership status</li>
 *   <li><strong>Hierarchy Queries:</strong> Retrieve fragments by abstraction level</li>
 *   <li><strong>Quality Queries:</strong> Filter fragments by importance scores</li>
 *   <li><strong>Lifecycle Queries:</strong> Support cleanup and maintenance operations</li>
 * </ul>
 * 
 * <p>The repository supports the memory system's core operations including:</p>
 * <ul>
 *   <li>Clustering of uncategorized fragments</li>
 *   <li>Hierarchical abstraction generation</li>
 *   <li>Importance-based filtering and cleanup</li>
 *   <li>Cross-cluster analysis and navigation</li>
 * </ul>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@ApplicationScoped
public class MemoryFragmentRepository implements PanacheRepository<MemoryFragment> {
    
    /**
     * Finds all memory fragments that have not yet been assigned to a cluster.
     * 
     * <p>This method is primarily used by the clustering service to identify
     * new memory fragments that need to be processed and grouped with semantically
     * similar content. Only returns original fragments (abstraction level = 1)
     * to avoid clustering already-processed abstractions.</p>
     * 
     * <p>Criteria for unclustered fragments:</p>
     * <ul>
     *   <li>clusterId is null (not assigned to any cluster)</li>
     *   <li>abstractionLevel = 1 (original content, not abstractions)</li>
     * </ul>
     * 
     * @return List of unclustered memory fragments ready for clustering
     */
    public List<MemoryFragment> findUnclustered() {
        return find("clusterId is null AND abstractionLevel = 1").list();
    }
    
    /**
     * Finds all memory fragments belonging to a specific cluster.
     * 
     * <p>This method retrieves all fragments that have been assigned to the
     * specified cluster ID. Used for cluster-based operations such as:</p>
     * <ul>
     *   <li>Generating abstractions from cluster members</li>
     *   <li>Cluster-aware retrieval operations</li>
     *   <li>Analyzing cluster composition and quality</li>
     *   <li>Updating cluster prototype vectors</li>
     * </ul>
     * 
     * @param clusterId The cluster ID to search for
     * @return List of memory fragments in the specified cluster
     */
    public List<MemoryFragment> findByClusterId(String clusterId) {
        return find("clusterId", clusterId).list();
    }
    
    /**
     * Finds memory fragments with importance scores above a threshold.
     * 
     * <p>This method filters fragments based on their calculated importance scores,
     * which are used for prioritization in various memory operations. High-importance
     * fragments are typically:</p>
     * <ul>
     *   <li>Preserved during cleanup operations</li>
     *   <li>Prioritized in retrieval ranking</li>
     *   <li>Considered for long-term retention</li>
     *   <li>Used as seeds for abstraction generation</li>
     * </ul>
     * 
     * @param minImportance Minimum importance score (0.0 to 1.0)
     * @return List of memory fragments with importance >= minImportance
     */
    public List<MemoryFragment> findByImportanceAbove(double minImportance) {
        return find("importanceScore >= ?1", minImportance).list();
    }
    
    /**
     * Finds memory fragments at a specific abstraction level.
     * 
     * <p>This method retrieves fragments based on their position in the memory
     * hierarchy. Different abstraction levels serve different purposes:</p>
     * <ul>
     *   <li><strong>Level 1:</strong> Original conversation fragments</li>
     *   <li><strong>Level 2:</strong> First-level abstractions (cluster summaries)</li>
     *   <li><strong>Level 3+:</strong> Higher-level abstractions (meta-summaries)</li>
     * </ul>
     * 
     * <p>Used for hierarchy-aware operations and abstraction chain analysis.</p>
     * 
     * @param abstractionLevel The abstraction level to search for (1 for original, 2+ for abstractions)
     * @return List of memory fragments at the specified abstraction level
     */
    public List<MemoryFragment> findByAbstractionLevel(int abstractionLevel) {
        return find("abstractionLevel", abstractionLevel).list();
    }
    
    /**
     * Finds all abstracted memory fragments (level > 1).
     * 
     * <p>This method retrieves all fragments that are abstractions rather than
     * original content. Abstracted fragments are AI-generated summaries that
     * condense information from multiple related original fragments.</p>
     * 
     * <p>Used for operations involving abstract content such as:</p>
     * <ul>
     *   <li>Abstraction quality analysis</li>
     *   <li>High-level semantic navigation</li>
     *   <li>Hierarchical cleanup strategies</li>
     *   <li>Meta-abstraction generation</li>
     * </ul>
     * 
     * @return List of abstracted memory fragments (abstraction level > 1)
     */
    public List<MemoryFragment> findAbstractions() {
        return find("abstractionLevel > 1").list();
    }
} 