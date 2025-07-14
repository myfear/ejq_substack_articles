package org.acme.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entity representing a memory fragment in the AI memory system.
 * 
 * <p>A MemoryFragment is the fundamental unit of memory storage, representing either
 * original conversational content or higher-level abstractions derived from clustering
 * and summarization processes. Each fragment contains both semantic (embedding) and
 * textual representations along with metadata for tracking usage patterns and hierarchical
 * relationships.</p>
 * 
 * <p>Key characteristics:</p>
 * <ul>
 *   <li><strong>Dual Storage:</strong> Maintains both high-precision embeddings for search
 *       and compressed quantized versions for efficient storage</li>
 *   <li><strong>Hierarchical Structure:</strong> Supports parent-child relationships for
 *       multi-level abstractions</li>
 *   <li><strong>Access Tracking:</strong> Records usage patterns for intelligent cleanup
 *       and retrieval optimization</li>
 *   <li><strong>Cluster Membership:</strong> Can belong to semantic clusters for grouped
 *       retrieval and abstraction</li>
 *   <li><strong>Importance Scoring:</strong> Maintains calculated importance scores for
 *       prioritization and cleanup decisions</li>
 * </ul>
 * 
 * <p>Abstraction levels indicate the hierarchical position:</p>
 * <ul>
 *   <li><strong>Level 1:</strong> Original conversation fragments</li>
 *   <li><strong>Level 2+:</strong> AI-generated abstractions and summaries</li>
 * </ul>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@Entity
@Table(name = "memory_fragments")
public class MemoryFragment extends PanacheEntity {

    /**
     * The original text content of this memory fragment.
     * 
     * <p>For level 1 fragments, this contains the raw conversational text.
     * For higher abstraction levels, this contains AI-generated summaries
     * that condense multiple related fragments into essential concepts.</p>
     */
    @Column(name = "original_text", columnDefinition = "TEXT")
    private String originalText;

    /**
     * High-precision embedding vector for semantic similarity search.
     * 
     * <p>This 384-dimensional vector captures the semantic meaning of the text
     * content using a pre-trained embedding model. Used for:
     * <ul>
     *   <li>Semantic similarity searches</li>
     *   <li>Clustering operations</li>
     *   <li>Relevance scoring</li>
     *   <li>Abstraction generation</li>
     * </ul>
     * 
     * <p>Stored as PostgreSQL VECTOR type for efficient similarity operations.</p>
     */
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 384) // Must match the embedding model's dimension
    @Column(name = "embedding")
    private float[] embedding;

    /**
     * Compressed embedding representation for efficient storage and archival.
     * 
     * <p>This scalar-quantized version of the embedding reduces storage requirements
     * while preserving approximate semantic information. Used for:
     * <ul>
     *   <li>Long-term archival storage</li>
     *   <li>Memory-constrained operations</li>
     *   <li>Approximate similarity when precision isn't critical</li>
     * </ul>
     */
    @Column(name = "quantized_embedding")
    private byte quantizedEmbedding;

    /**
     * The hierarchical abstraction level of this memory fragment.
     * 
     * <p>Indicates the position in the memory hierarchy:
     * <ul>
     *   <li><strong>1:</strong> Original conversation fragments</li>
     *   <li><strong>2:</strong> First-level abstractions (cluster summaries)</li>
     *   <li><strong>3+:</strong> Higher-level abstractions (summaries of summaries)</li>
     * </ul>
     * 
     * <p>Higher levels represent more condensed and generalized information,
     * while lower levels contain more specific details.</p>
     */
    @Column(name = "abstraction_level")
    private Integer abstractionLevel = 1;

    /**
     * Calculated importance score for this memory fragment.
     * 
     * <p>A normalized score (0.0 to 1.0) indicating the relative importance
     * of this memory fragment. Factors influencing the score include:
     * <ul>
     *   <li>Access frequency and recency</li>
     *   <li>Semantic uniqueness</li>
     *   <li>Position in abstraction hierarchy</li>
     *   <li>Cluster membership size</li>
     *   <li>User interaction patterns</li>
     * </ul>
     * 
     * <p>Used by cleanup algorithms to preserve important memories and
     * by retrieval systems for relevance ranking.</p>
     */
    @Column(name = "importance_score")
    private Double importanceScore;

    /**
     * Identifier of the semantic cluster this fragment belongs to.
     * 
     * <p>Memory fragments with similar semantic content are grouped into
     * clusters using DBSCAN algorithm. The cluster ID allows for:
     * <ul>
     *   <li>Efficient cluster-based retrieval</li>
     *   <li>Abstraction generation from related memories</li>
     *   <li>Grouped cleanup operations</li>
     *   <li>Semantic navigation and exploration</li>
     * </ul>
     * 
     * <p>Null indicates the fragment hasn't been clustered yet or
     * was classified as noise by the clustering algorithm.</p>
     */
    @Column(name = "cluster_id")
    private String clusterId;

    /**
     * Timestamp when this memory fragment was originally created.
     * 
     * <p>Used for:
     * <ul>
     *   <li>Age-based cleanup policies</li>
     *   <li>Temporal analysis of memory patterns</li>
     *   <li>Chronological sorting and filtering</li>
     *   <li>Recency scoring in retrieval algorithms</li>
     * </ul>
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Timestamp when this memory fragment was last accessed or retrieved.
     * 
     * <p>Updated automatically by the retrieval system when this fragment
     * is returned in search results. Used for:
     * <ul>
     *   <li>Access pattern analysis</li>
     *   <li>Recency-based relevance scoring</li>
     *   <li>Usage-based cleanup decisions</li>
     *   <li>Performance optimization</li>
     * </ul>
     */
    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    /**
     * Number of times this memory fragment has been accessed or retrieved.
     * 
     * <p>Incremented each time the fragment appears in search results.
     * Used for:
     * <ul>
     *   <li>Popularity-based ranking</li>
     *   <li>Frequency analysis</li>
     *   <li>Cleanup prioritization</li>
     *   <li>Importance score calculation</li>
     * </ul>
     */
    @Column(name = "access_count")
    private Integer accessCount = 0;

    /**
     * Parent memory fragment in the abstraction hierarchy.
     * 
     * <p>References a higher-level abstraction that this fragment contributes to.
     * For example, original conversation fragments (level 1) may have a parent
     * abstraction (level 2) that summarizes their cluster.</p>
     * 
     * <p>Lazy loading is used to avoid unnecessary database queries when
     * accessing fragment collections.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_memory_id")
    private MemoryFragment parentMemory;

    /**
     * Child memory fragments that this abstraction summarizes.
     * 
     * <p>For abstraction fragments (level > 1), this contains the collection
     * of lower-level fragments that were used to generate this summary.
     * Cascade operations ensure that when an abstraction is deleted,
     * the parent-child relationships are properly maintained.</p>
     * 
     * <p>Lazy loading optimizes performance by loading children only when needed.</p>
     */
    @OneToMany(mappedBy = "parentMemory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MemoryFragment> childMemories = new ArrayList<>();

    // Getter and setter methods with comprehensive documentation

    /**
     * Gets the original text content of this memory fragment.
     * 
     * @return the original text content, or null if not set
     */
    public String getOriginalText() {
        return originalText;
    }

    /**
     * Sets the original text content of this memory fragment.
     * 
     * @param originalText the text content to set
     */
    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    /**
     * Gets the high-precision embedding vector for semantic operations.
     * 
     * @return the 384-dimensional embedding array, or null if not generated
     */
    public float[] getEmbedding() {
        return embedding;
    }

    /**
     * Sets the high-precision embedding vector.
     * 
     * @param embedding the 384-dimensional embedding array
     */
    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    /**
     * Gets the quantized embedding for efficient storage.
     * 
     * @return the compressed embedding byte value
     */
    public byte getQuantizedEmbedding() {
        return quantizedEmbedding;
    }

    /**
     * Sets the quantized embedding.
     * 
     * @param quantizedEmbedding the compressed embedding byte value
     */
    public void setQuantizedEmbedding(byte quantizedEmbedding) {
        this.quantizedEmbedding = quantizedEmbedding;
    }

    /**
     * Gets the abstraction level of this memory fragment.
     * 
     * @return the abstraction level (1 for original, 2+ for abstractions)
     */
    public Integer getAbstractionLevel() {
        return abstractionLevel;
    }

    /**
     * Sets the abstraction level of this memory fragment.
     * 
     * @param abstractionLevel the abstraction level to set
     */
    public void setAbstractionLevel(Integer abstractionLevel) {
        this.abstractionLevel = abstractionLevel;
    }

    /**
     * Gets the calculated importance score.
     * 
     * @return the importance score (0.0 to 1.0), or null if not calculated
     */
    public Double getImportanceScore() {
        return importanceScore;
    }

    /**
     * Sets the calculated importance score.
     * 
     * @param importanceScore the importance score (should be 0.0 to 1.0)
     */
    public void setImportanceScore(Double importanceScore) {
        this.importanceScore = importanceScore;
    }

    /**
     * Gets the cluster ID this fragment belongs to.
     * 
     * @return the cluster ID, or null if not clustered
     */
    public String getClusterId() {
        return clusterId;
    }

    /**
     * Sets the cluster ID this fragment belongs to.
     * 
     * @param clusterId the cluster ID to set, or null to remove from cluster
     */
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    /**
     * Gets the creation timestamp.
     * 
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     * 
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last accessed timestamp.
     * 
     * @return the last accessed timestamp
     */
    public LocalDateTime getLastAccessed() {
        return lastAccessed;
    }

    /**
     * Sets the last accessed timestamp.
     * 
     * @param lastAccessed the last accessed timestamp to set
     */
    public void setLastAccessed(LocalDateTime lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    /**
     * Gets the access count.
     * 
     * @return the number of times this fragment has been accessed
     */
    public Integer getAccessCount() {
        return accessCount;
    }

    /**
     * Sets the access count.
     * 
     * @param accessCount the access count to set
     */
    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }

    /**
     * Gets the parent memory fragment in the abstraction hierarchy.
     * 
     * @return the parent memory fragment, or null if this is a top-level fragment
     */
    public MemoryFragment getParentMemory() {
        return parentMemory;
    }

    /**
     * Sets the parent memory fragment in the abstraction hierarchy.
     * 
     * @param parentMemory the parent memory fragment to set
     */
    public void setParentMemory(MemoryFragment parentMemory) {
        this.parentMemory = parentMemory;
    }

    /**
     * Gets the list of child memory fragments that this abstraction summarizes.
     * 
     * @return the list of child memory fragments (may be empty)
     */
    public List<MemoryFragment> getChildMemories() {
        return childMemories;
    }

    /**
     * Sets the list of child memory fragments that this abstraction summarizes.
     * 
     * @param childMemories the list of child memory fragments to set
     */
    public void setChildMemories(List<MemoryFragment> childMemories) {
        this.childMemories = childMemories;
    }
}
