package org.acme.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.acme.entities.MemoryCluster;
import org.acme.entities.MemoryFragment;
import org.acme.repositories.MemoryClusterRepository;
import org.acme.repositories.MemoryFragmentRepository;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service responsible for retrieving and ranking relevant memory fragments
 * based on semantic similarity.
 * 
 * <p>
 * This service provides sophisticated memory retrieval capabilities using
 * multiple strategies:
 * </p>
 * <ul>
 * <li><strong>Semantic Search:</strong> Uses embedding similarity to find
 * contextually relevant memories</li>
 * <li><strong>Cluster-Aware Retrieval:</strong> Leverages memory clusters for
 * efficient grouped retrieval</li>
 * <li><strong>Intelligent Ranking:</strong> Combines relevance, recency, and
 * access patterns for optimal results</li>
 * <li><strong>Access Tracking:</strong> Monitors memory usage patterns to
 * improve future retrieval</li>
 * </ul>
 * 
 * <p>
 * The service supports both direct memory retrieval and cluster-based
 * retrieval, allowing for
 * flexible retrieval strategies based on the query context and performance
 * requirements.
 * </p>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@ApplicationScoped
public class MemoryRetrievalService {

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Inject
    MemoryFragmentRepository memoryFragmentRepository;

    @Inject
    MemoryClusterRepository clusterRepository;

    /**
     * Finds relevant memory candidates using basic semantic similarity search.
     * 
     * <p>
     * This method performs a straightforward embedding-based search to find
     * memories
     * that are semantically similar to the given query. It's typically used as a
     * starting
     * point for more sophisticated retrieval operations.
     * </p>
     * 
     * @param query      the search query to find relevant memories for
     * @param maxResults maximum number of results to return
     * @return List of EmbeddingMatch objects representing relevant memory
     *         candidates
     * @throws RuntimeException if embedding generation or search fails
     */
    public List<EmbeddingMatch<TextSegment>> findRelevantMemoryCandidates(String query, int maxResults) {
        Log.infof("Retrieval: Searching for relevant memories for query: '%s'", query);

        // 1. Embed the incoming query
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        Log.infof("Retrieval: Generated query embedding with %d dimensions", queryEmbedding.vector().length);

        // 2. Search the embedding store for similar memories
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .minScore(0.5) // Only return memories with at least 50% similarity
                .build();

        List<EmbeddingMatch<TextSegment>> candidates = embeddingStore.search(searchRequest).matches();
        Log.infof("Retrieval: Found %d memory candidates with similarity >= 0.5", candidates.size());

        // 3. Log details for debugging
        for (int i = 0; i < Math.min(candidates.size(), 3); i++) {
            EmbeddingMatch<TextSegment> candidate = candidates.get(i);
            String text = candidate.embedded().text();
            Log.infof("Retrieval: Candidate %d (score: %.3f): %s",
                    i + 1, candidate.score(), text.substring(0, Math.min(60, text.length())));
        }

        return candidates;
    }

    /**
     * Container class for scored memory results used in intelligent ranking.
     * 
     * <p>
     * This record combines an embedding match with a calculated relevance score
     * that takes into account multiple factors including semantic similarity,
     * recency, and access patterns.
     * </p>
     * 
     * @param match          the original embedding match result
     * @param relevanceScore the calculated composite relevance score
     */
    private static class ScoredMemory {
        final EmbeddingMatch<TextSegment> match;
        final double relevanceScore;

        public ScoredMemory(EmbeddingMatch<TextSegment> match, double relevanceScore) {
            this.match = match;
            this.relevanceScore = relevanceScore;
        }

        public EmbeddingMatch<TextSegment> match() {
            return match;
        }

        public double relevanceScore() {
            return relevanceScore;
        }
    }

    /**
     * Retrieves and ranks memory fragments using sophisticated scoring algorithms.
     * 
     * <p>
     * This method implements the core memory retrieval logic with intelligent
     * ranking
     * that considers multiple factors:
     * </p>
     * <ul>
     * <li><strong>Semantic Similarity:</strong> Base similarity score from
     * embedding comparison</li>
     * <li><strong>Recency Score:</strong> Bonus for recently accessed or created
     * memories</li>
     * <li><strong>Access Patterns:</strong> Boost for frequently accessed
     * memories</li>
     * <li><strong>Importance Score:</strong> Considers the calculated importance of
     * memory fragments</li>
     * </ul>
     * 
     * <p>
     * The method also includes intelligent filtering to avoid redundant results and
     * updates access tracking for retrieved memories.
     * </p>
     * 
     * @param query the search query to find relevant memories for
     * @param limit maximum number of results to return
     * @return List of ranked EmbeddingMatch objects representing the most relevant
     *         memories
     * @throws RuntimeException if retrieval or ranking fails
     */
    @Transactional
    public List<EmbeddingMatch<TextSegment>> retrieveAndRankMemories(String query, int limit) {
        Log.infof("Retrieval: Starting advanced memory retrieval for query: '%s'", query);

        // Step 1: Use cluster-aware retrieval for better results
        List<EmbeddingMatch<TextSegment>> candidates = findRelevantClustersAndMembers(query, limit * 3);

        if (candidates.isEmpty()) {
            Log.infof("Retrieval: No candidates found via cluster-aware retrieval");
            return new ArrayList<>();
        }

        Log.infof("Retrieval: Found %d candidates from cluster-aware retrieval", candidates.size());

        // Step 2: Calculate enhanced relevance scores
        List<ScoredMemory> scoredResults = candidates.stream()
                .map(match -> {
                    double baseScore = match.score();
                    double relevanceScore = calculateRelevanceScore(match);
                    double totalScore = baseScore * 0.6 + relevanceScore * 0.4;

                    Log.infof("Retrieval: Memory score calculation - base: %.3f, relevance: %.3f, total: %.3f",
                            baseScore, relevanceScore, totalScore);

                    return new ScoredMemory(match, totalScore);
                })
                .sorted(Comparator.comparingDouble(ScoredMemory::relevanceScore).reversed())
                .collect(Collectors.toList());

        // Step 3: Apply intelligent filtering and selection
        List<EmbeddingMatch<TextSegment>> finalResults = applyIntelligentFiltering(scoredResults, limit);

        Log.infof("Retrieval: Final ranking produced %d results", finalResults.size());

        // Step 4: Update access tracking for retrieved memories
        updateAccessTracking(finalResults);

        return finalResults;
    }

    /**
     * Applies intelligent filtering to avoid redundant results and improve
     * diversity.
     * 
     * <p>
     * This method implements sophisticated filtering logic to ensure result
     * quality:
     * </p>
     * <ul>
     * <li>Removes near-duplicate memories based on text similarity</li>
     * <li>Ensures diversity in result set</li>
     * <li>Applies threshold-based filtering for quality control</li>
     * <li>Maintains result ordering based on relevance scores</li>
     * </ul>
     * 
     * @param scoredResults list of scored memory results to filter
     * @param limit         maximum number of results to return
     * @return List of filtered and ranked EmbeddingMatch objects
     */
    private List<EmbeddingMatch<TextSegment>> applyIntelligentFiltering(List<ScoredMemory> scoredResults, int limit) {
        Log.infof("Retrieval: Applying intelligent filtering to %d scored results", scoredResults.size());

        List<EmbeddingMatch<TextSegment>> filteredResults = new ArrayList<>();
        Set<String> seenContent = new HashSet<>();

        for (ScoredMemory scoredMemory : scoredResults) {
            if (filteredResults.size() >= limit) {
                break;
            }

            EmbeddingMatch<TextSegment> match = scoredMemory.match();
            String content = match.embedded().text();

            // Simple deduplication based on text content
            String contentKey = content.toLowerCase().trim();
            if (seenContent.contains(contentKey)) {
                Log.infof("Retrieval: Skipping duplicate content: %s",
                        content.substring(0, Math.min(50, content.length())));
                continue;
            }

            // Apply minimum score threshold
            if (scoredMemory.relevanceScore() < 0.3) {
                Log.infof("Retrieval: Skipping low-relevance result (score: %.3f)", scoredMemory.relevanceScore());
                continue;
            }

            seenContent.add(contentKey);
            filteredResults.add(match);

            Log.infof("Retrieval: Added result %d (score: %.3f): %s",
                    filteredResults.size(), scoredMemory.relevanceScore(),
                    content.substring(0, Math.min(60, content.length())));
        }

        Log.infof("Retrieval: Intelligent filtering reduced %d results to %d", scoredResults.size(),
                filteredResults.size());
        return filteredResults;
    }

    /**
     * Calculates a composite relevance score for a memory fragment.
     * 
     * <p>
     * The relevance score combines multiple factors to provide a more accurate
     * assessment of memory relevance than simple embedding similarity alone:
     * </p>
     * <ul>
     * <li><strong>Recency Bonus:</strong> Recently accessed memories get higher
     * scores</li>
     * <li><strong>Access Pattern Bonus:</strong> Frequently accessed memories get
     * higher scores</li>
     * <li><strong>Importance Score:</strong> Memories with higher importance get
     * higher scores</li>
     * </ul>
     * 
     * @param match the embedding match to calculate relevance score for
     * @return double representing the composite relevance score (0.0 to 1.0)
     */
    private double calculateRelevanceScore(EmbeddingMatch<TextSegment> match) {
        // Base relevance is the embedding similarity score
        double baseScore = match.score();

        // Extract metadata for additional scoring factors
        Metadata metadata = match.embedded().metadata();

        // Recency boost - recently accessed memories are more relevant
        String lastAccessedStr = metadata.getString("lastAccessed");
        double recencyScore = 0.0;
        if (lastAccessedStr != null) {
            try {
                LocalDateTime lastAccessed = LocalDateTime.parse(lastAccessedStr);
                recencyScore = calculateRecencyScore(lastAccessed);
            } catch (Exception e) {
                Log.warnf("Retrieval: Failed to parse lastAccessed: %s", lastAccessedStr);
            }
        }

        // Access pattern boost - frequently accessed memories are more relevant
        String accessCountStr = metadata.getString("accessCount");
        double accessBoost = 0.0;
        if (accessCountStr != null) {
            try {
                int accessCount = Integer.parseInt(accessCountStr);
                accessBoost = Math.min(0.2, accessCount * 0.02); // Cap at 20% boost
            } catch (Exception e) {
                Log.warnf("Retrieval: Failed to parse accessCount: %s", accessCountStr);
            }
        }

        // Importance boost based on stored importance score
        String importanceStr = metadata.getString("importanceScore");
        double importanceBoost = 0.0;
        if (importanceStr != null) {
            try {
                double importance = Double.parseDouble(importanceStr);
                importanceBoost = (importance - 0.5) * 0.1; // Scale around 0.5 baseline
            } catch (Exception e) {
                Log.warnf("Retrieval: Failed to parse importanceScore: %s", importanceStr);
            }
        }

        return baseScore + recencyScore + accessBoost + importanceBoost;
    }

    /**
     * Calculates a recency score based on when a memory was last accessed.
     * 
     * <p>
     * The recency score provides a time-based boost to recently accessed memories,
     * with the score decreasing exponentially as time passes. This ensures that
     * recently relevant memories remain easily accessible.
     * </p>
     * 
     * @param lastAccessed the timestamp when the memory was last accessed
     * @return double representing the recency score (0.0 to 0.15)
     */
    private double calculateRecencyScore(LocalDateTime lastAccessed) {
        LocalDateTime now = LocalDateTime.now();
        long hoursAgo = java.time.Duration.between(lastAccessed, now).toHours();

        // Exponential decay: recent memories get higher scores
        // Score drops to ~0.05 after 24 hours, ~0.01 after 48 hours
        double recencyScore = 0.15 * Math.exp(-hoursAgo / 24.0);

        return recencyScore;
    }

    /**
     * Reconstructs meaningful context from a set of ranked memory fragments.
     * 
     * <p>
     * This method takes retrieved memory fragments and combines them into a
     * coherent context string that can be used for conversation or further
     * processing.
     * The context is built by extracting and organizing the text content from
     * the memory fragments while preserving their relevance order.
     * </p>
     * 
     * @param rankedMemories list of ranked memory fragments to reconstruct context
     *                       from
     * @return String containing the reconstructed memory context
     */
    public String reconstructMemoryContext(List<EmbeddingMatch<TextSegment>> rankedMemories) {
        if (rankedMemories.isEmpty()) {
            return "No relevant memories found.";
        }

        Log.infof("Retrieval: Reconstructing context from %d ranked memories", rankedMemories.size());

        StringBuilder context = new StringBuilder();
        context.append("Relevant memories:\n");

        for (int i = 0; i < rankedMemories.size(); i++) {
            EmbeddingMatch<TextSegment> match = rankedMemories.get(i);
            String text = match.embedded().text();

            context.append(String.format("%d. %s\n", i + 1, text));
        }

        String finalContext = context.toString();
        Log.infof("Retrieval: Reconstructed context length: %d characters", finalContext.length());

        return finalContext;
    }



    /**
     * Performs cluster-aware retrieval to find relevant memories and their cluster
     * members.
     * 
     * <p>
     * This method implements a sophisticated retrieval strategy that leverages
     * the clustering structure of memories. The process involves:
     * </p>
     * <ol>
     * <li>Finding clusters that are semantically similar to the query</li>
     * <li>Retrieving all members of the most relevant clusters</li>
     * <li>Combining cluster-based results with direct semantic search</li>
     * <li>Ranking results based on both cluster similarity and individual
     * relevance</li>
     * </ol>
     * 
     * <p>
     * This approach is particularly effective for finding related memories that
     * might not be individually similar to the query but belong to relevant
     * clusters.
     * </p>
     * 
     * @param query the search query to find relevant memories for
     * @param limit maximum number of results to return
     * @return List of EmbeddingMatch objects representing relevant memories from
     *         clusters
     */
    private List<EmbeddingMatch<TextSegment>> findRelevantClustersAndMembers(String query, int limit) {
        Log.infof("Retrieval: Starting cluster-aware retrieval for query: '%s'", query);

        // 1. Generate embedding for the query
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        float[] queryVector = queryEmbedding.vector();

        // 2. Find clusters similar to the query
        List<MemoryCluster> allClusters = clusterRepository.listAll();
        Log.infof("Retrieval: Evaluating %d clusters for query relevance", allClusters.size());

        List<ClusterScore> clusterScores = new ArrayList<>();

        for (MemoryCluster cluster : allClusters) {
            float[] prototypeVector = cluster.getPrototypeVector();
            if (prototypeVector != null && prototypeVector.length > 0) {
                double similarity = calculateCosineSimilarity(queryVector, prototypeVector);
                clusterScores.add(new ClusterScore(cluster, similarity));

                Log.infof("Retrieval: Cluster %s (theme: %s) similarity: %.3f",
                        cluster.getClusterId(), cluster.getTheme(), similarity);
            }
        }

        // 3. Sort clusters by similarity and take top ones
        clusterScores.sort(Comparator.comparingDouble(cs -> -cs.similarity));

        // 4. Retrieve members from top clusters
        List<EmbeddingMatch<TextSegment>> results = new ArrayList<>();
        Set<String> addedMemoryIds = new HashSet<>();

        int clustersToProcess = Math.min(3, clusterScores.size()); // Process top 3 clusters
        for (int i = 0; i < clustersToProcess; i++) {
            ClusterScore clusterScore = clusterScores.get(i);
            MemoryCluster cluster = clusterScore.cluster;

            if (clusterScore.similarity < 0.6) {
                Log.infof("Retrieval: Skipping cluster %s due to low similarity (%.3f)",
                        cluster.getClusterId(), clusterScore.similarity);
                continue;
            }

            Log.infof("Retrieval: Processing cluster %s (similarity: %.3f, theme: %s)",
                    cluster.getClusterId(), clusterScore.similarity, cluster.getTheme());

            // Find all memories in this cluster
            List<MemoryFragment> clusterMembers = memoryFragmentRepository.findByClusterId(cluster.getClusterId());

            for (MemoryFragment member : clusterMembers) {
                if (addedMemoryIds.contains(member.id.toString())) {
                    continue; // Skip duplicates
                }

                // Calculate similarity between query and this specific memory
                float[] memberEmbedding = member.getEmbedding();
                if (memberEmbedding != null) {
                    double memberSimilarity = calculateCosineSimilarity(queryVector, memberEmbedding);

                    // Boost similarity based on cluster relevance
                    double boostedSimilarity = memberSimilarity * 0.8 + clusterScore.similarity * 0.2;

                    // Create TextSegment with metadata
                    Metadata metadata = new Metadata();
                    metadata.put("id", member.id.toString());
                    metadata.put("clusterId", cluster.getClusterId());
                    if (cluster.getTheme() != null) {
                        metadata.put("clusterTheme", cluster.getTheme());
                    }
                    if (member.getLastAccessed() != null) {
                        metadata.put("lastAccessed", member.getLastAccessed().toString());
                    }
                    if (member.getAccessCount() != null) {
                        metadata.put("accessCount", member.getAccessCount().toString());
                    }
                    if (member.getImportanceScore() != null) {
                        metadata.put("importanceScore", member.getImportanceScore().toString());
                    }
                    if (member.getAbstractionLevel() != null) {
                        metadata.put("abstractionLevel", member.getAbstractionLevel().toString());
                    }

                    TextSegment textSegment = TextSegment.from(member.getOriginalText(), metadata);
                    EmbeddingMatch<TextSegment> match = new EmbeddingMatch<>(
                            boostedSimilarity,
                            member.id.toString(), // Use member ID as embeddingId
                            Embedding.from(memberEmbedding),
                            textSegment);

                    results.add(match);
                    addedMemoryIds.add(member.id.toString());

                    Log.infof("Retrieval: Added cluster member (similarity: %.3f, boosted: %.3f): %s",
                            memberSimilarity, boostedSimilarity,
                            member.getOriginalText().substring(0, Math.min(60, member.getOriginalText().length())));
                }
            }
        }

        // 5. Sort results by boosted similarity and return top ones
        results.sort(Comparator.comparingDouble(match -> -match.score()));

        List<EmbeddingMatch<TextSegment>> limitedResults = results.stream()
                .limit(limit)
                .collect(Collectors.toList());

        Log.infof("Retrieval: Cluster-aware retrieval returned %d results from %d clusters",
                limitedResults.size(), clustersToProcess);

        return limitedResults;
    }

    /**
     * Calculates cosine similarity between two vectors.
     * 
     * <p>
     * Cosine similarity is a measure of similarity between two non-zero vectors
     * defined in an inner product space. It measures the cosine of the angle
     * between
     * the vectors, providing a value between -1 and 1 where 1 indicates identical
     * direction, 0 indicates orthogonal vectors, and -1 indicates opposite
     * directions.
     * </p>
     * 
     * @param vectorA first vector for comparison
     * @param vectorB second vector for comparison
     * @return double representing the cosine similarity (-1.0 to 1.0)
     */
    private double calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vector dimensions must match");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Container class for cluster similarity scores used in cluster-aware
     * retrieval.
     * 
     * @param cluster    the memory cluster being scored
     * @param similarity the calculated similarity score between cluster and query
     */
    private static class ClusterScore {
        final MemoryCluster cluster;
        final double similarity;

        ClusterScore(MemoryCluster cluster, double similarity) {
            this.cluster = cluster;
            this.similarity = similarity;
        }
    }

    /**
     * Updates access tracking information for retrieved memory fragments.
     * 
     * <p>
     * This method updates the access patterns for memories that were retrieved
     * and returned to the user. It increments access counts and updates last
     * accessed timestamps, which are used for future relevance calculations.
     * </p>
     * 
     * @param retrievedMemories list of memory fragments that were retrieved
     */
    private void updateAccessTracking(List<EmbeddingMatch<TextSegment>> retrievedMemories) {
        Log.infof("Retrieval: Updating access tracking for %d retrieved memories", retrievedMemories.size());

        for (EmbeddingMatch<TextSegment> match : retrievedMemories) {
            String memoryId = match.embedded().metadata().getString("id");
            if (memoryId != null) {
                try {
                    Long id = Long.parseLong(memoryId);
                    MemoryFragment fragment = memoryFragmentRepository.findById(id);
                    if (fragment != null) {
                        fragment.setAccessCount(fragment.getAccessCount() + 1);
                        fragment.setLastAccessed(LocalDateTime.now());
                        Log.infof("Retrieval: Updated access tracking for memory %s (new count: %d)",
                                memoryId, fragment.getAccessCount());
                    }
                } catch (NumberFormatException e) {
                    Log.warnf("Retrieval: Invalid memory ID format: %s", memoryId);
                }
            }
        }
    }
}
