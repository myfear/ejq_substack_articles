package org.acme.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.acme.entities.MemoryCluster;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository for managing memory cluster data access operations.
 * 
 * <p>This repository provides specialized query methods for retrieving and analyzing
 * memory clusters within the AI memory system. It supports various cluster-related
 * operations including cluster lifecycle management, maturity assessment, and
 * content-based filtering.</p>
 * 
 * <p>Key query categories:</p>
 * <ul>
 *   <li><strong>Identity Queries:</strong> Find clusters by unique identifiers</li>
 *   <li><strong>Size-based Queries:</strong> Filter clusters by member count thresholds</li>
 *   <li><strong>Temporal Queries:</strong> Retrieve clusters based on update patterns</li>
 *   <li><strong>Content Queries:</strong> Search clusters by semantic themes</li>
 *   <li><strong>Maturity Queries:</strong> Identify clusters ready for abstraction</li>
 * </ul>
 * 
 * <p>The repository supports critical memory system operations including:</p>
 * <ul>
 *   <li>Cluster-aware memory retrieval</li>
 *   <li>Abstraction candidate identification</li>
 *   <li>Cluster analysis and monitoring</li>
 *   <li>Theme-based semantic navigation</li>
 * </ul>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@ApplicationScoped
public class MemoryClusterRepository implements PanacheRepository<MemoryCluster> {
    
    /**
     * Finds a cluster by its unique cluster identifier.
     * 
     * <p>This method provides direct access to clusters using their UUID-based
     * cluster ID. The cluster ID is the primary way to reference clusters from
     * memory fragments and other system components.</p>
     * 
     * @param clusterId The unique cluster ID to search for
     * @return Optional containing the cluster if found, empty otherwise
     */
    public Optional<MemoryCluster> findByClusterId(String clusterId) {
        return find("clusterId", clusterId).firstResultOptional();
    }
    
    /**
     * Finds clusters with member count above a specified threshold.
     * 
     * <p>This method filters clusters based on their size, which is useful for:</p>
     * <ul>
     *   <li>Identifying significant clusters for abstraction</li>
     *   <li>Analyzing cluster distribution patterns</li>
     *   <li>Prioritizing large clusters for processing</li>
     *   <li>Quality assessment of clustering results</li>
     * </ul>
     * 
     * <p>Larger clusters typically represent more stable semantic groupings
     * and are better candidates for abstraction generation.</p>
     * 
     * @param minMemberCount Minimum member count threshold
     * @return List of clusters with member count >= minMemberCount
     */
    public List<MemoryCluster> findByMemberCountAbove(int minMemberCount) {
        return find("memberCount >= ?1", minMemberCount).list();
    }
    
    /**
     * Finds clusters that have been updated since a specific date.
     * 
     * <p>This method tracks cluster evolution and identifies recently active clusters.
     * Used for:</p>
     * <ul>
     *   <li>Monitoring clustering algorithm progress</li>
     *   <li>Identifying clusters with recent membership changes</li>
     *   <li>Scheduling maintenance operations</li>
     *   <li>Analyzing temporal clustering patterns</li>
     * </ul>
     * 
     * @param since The date threshold to filter from
     * @return List of clusters updated after the specified date
     */
    public List<MemoryCluster> findUpdatedSince(LocalDateTime since) {
        return find("lastUpdated >= ?1", since).list();
    }
    
    /**
     * Finds clusters by searching for keywords in their semantic themes.
     * 
     * <p>This method enables content-based cluster discovery using the automatically
     * generated themes. Useful for:</p>
     * <ul>
     *   <li>Semantic navigation through memory clusters</li>
     *   <li>Topic-based cluster exploration</li>
     *   <li>Content analysis and categorization</li>
     *   <li>User-directed memory search</li>
     * </ul>
     * 
     * <p>Uses case-insensitive partial matching to find clusters whose themes
     * contain the specified keyword.</p>
     * 
     * @param keyword Keyword to search for in cluster themes
     * @return List of clusters containing the keyword in their theme
     */
    public List<MemoryCluster> findByThemeKeyword(String keyword) {
        return find("theme LIKE ?1", "%" + keyword + "%").list();
    }
    
    /**
     * Finds the most recently updated clusters.
     * 
     * <p>This method returns clusters ordered by their last update timestamp,
     * providing insight into recent clustering activity. Used for:</p>
     * <ul>
     *   <li>Monitoring real-time clustering progress</li>
     *   <li>Identifying recently formed or modified clusters</li>
     *   <li>Debugging clustering algorithm behavior</li>
     *   <li>Performance analysis and optimization</li>
     * </ul>
     * 
     * @param limit Maximum number of clusters to return
     * @return List of most recently updated clusters (newest first)
     */
    public List<MemoryCluster> findMostRecent(int limit) {
        return find("ORDER BY lastUpdated DESC").page(0, limit).list();
    }
    
    /**
     * Finds the largest clusters by member count.
     * 
     * <p>This method returns clusters ordered by their member count in descending
     * order, highlighting the most significant semantic groupings. Large clusters
     * often represent:</p>
     * <ul>
     *   <li>Major conversation topics or themes</li>
     *   <li>Stable semantic patterns in the memory system</li>
     *   <li>Prime candidates for abstraction generation</li>
     *   <li>Areas of high user engagement or interest</li>
     * </ul>
     * 
     * @param limit Maximum number of clusters to return
     * @return List of clusters ordered by member count (largest first)
     */
    public List<MemoryCluster> findLargestClusters(int limit) {
        return find("ORDER BY memberCount DESC").page(0, limit).list();
    }
    
    /**
     * Finds mature clusters that are ready for abstraction generation.
     * 
     * <p>This method identifies clusters that meet maturity criteria for abstraction
     * processing. Mature clusters are characterized by:</p>
     * <ul>
     *   <li><strong>Sufficient Size:</strong> >= 2 member fragments</li>
     *   <li><strong>Stability:</strong> Not updated in the last 30 seconds</li>
     * </ul>
     * 
     * <p>The stability requirement ensures that the cluster has settled and is
     * unlikely to change significantly in the near future, making it a good
     * candidate for abstraction generation.</p>
     * 
     * <p><strong>Note:</strong> The time threshold is currently set to 30 seconds
     * for testing purposes and should be adjusted to longer periods (hours) in
     * production environments.</p>
     * 
     * @return List of mature clusters ready for abstraction processing
     */
    public List<MemoryCluster> findMatureClusters() {
        // Temporarily reduced requirements for testing - 30 seconds instead of 1 hour
        LocalDateTime thirtySecondsAgo = LocalDateTime.now().minusSeconds(30);
        return find("memberCount >= 2 AND lastUpdated < ?1", thirtySecondsAgo).list();
    }
} 