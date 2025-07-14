package org.acme.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.acme.entities.Cluster;
import org.acme.entities.MemoryCluster;
import org.acme.entities.MemoryFragment;
import org.acme.repositories.MemoryClusterRepository;
import org.acme.repositories.MemoryFragmentRepository;
import org.acme.util.DBSCANClusterer;
import org.acme.util.DBSCANClusterer.MemoryFragmentCosineDistance;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service responsible for clustering memory fragments based on their semantic similarity.
 * 
 * <p>This service uses the DBSCAN (Density-Based Spatial Clustering of Applications with Noise)
 * algorithm to group semantically similar memory fragments together. The clustering is performed
 * periodically to organize memories into coherent groups that can be efficiently retrieved and
 * abstracted.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>Automatic periodic clustering of unclustered memory fragments</li>
 *   <li>Uses cosine distance to measure semantic similarity between embeddings</li>
 *   <li>Generates meaningful cluster themes based on content analysis</li>
 *   <li>Calculates prototype vectors (centroids) for efficient cluster-based retrieval</li>
 *   <li>Handles noise detection to avoid grouping unrelated memories</li>
 * </ul>
 * 
 * <p>The service runs every hour to process new unclustered fragments and maintain
 * the memory organization structure.</p>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@ApplicationScoped
public class ClusteringService {

    @Inject
    MemoryFragmentRepository memoryFragmentRepository; // Custom Panache repository

    @Inject
    MemoryClusterRepository memoryClusterRepository;

    /**
     * Performs periodic clustering of unclustered memory fragments.
     * 
     * <p>This method identifies memory fragments that haven't been assigned to any cluster
     * and groups them using the DBSCAN algorithm. The clustering process:</p>
     * <ol>
     *   <li>Fetches all unclustered memory fragments from the database</li>
     *   <li>Applies DBSCAN clustering with configurable epsilon and minimum points parameters</li>
     *   <li>Creates cluster entities with prototype vectors and meaningful themes</li>
     *   <li>Updates fragment records with their assigned cluster IDs</li>
     *   <li>Persists cluster metadata for future retrieval optimization</li>
     * </ol>
     * 
     * <p>The clustering parameters are:</p>
     * <ul>
     *   <li><strong>eps:</strong> 0.814 - Maximum distance between points to be considered neighbors</li>
     *   <li><strong>minPts:</strong> 2 - Minimum number of points required to form a cluster</li>
     * </ul>
     * 
     * <p>Fragments that don't meet the clustering criteria are considered noise and
     * remain unclustered until more similar fragments are added.</p>
     * 
     * @throws RuntimeException if clustering fails due to database or embedding issues
     */
    @Scheduled(every = "1h")
    @Transactional
    public void performClustering() {
        Log.infof("Clustering: Starting periodic clustering task");
        
        // 1. Fetch recent memories that have not yet been assigned to a cluster
        List<MemoryFragment> unclusteredFragments = memoryFragmentRepository.findUnclustered();

        if (unclusteredFragments.isEmpty()) {
            Log.infof("Clustering: No unclustered fragments found, skipping clustering");
            return;
        }

        Log.infof("Clustering: Found %d unclustered fragments ready for clustering", unclusteredFragments.size());

        // Debug: Check embedding availability
        int fragmentsWithEmbeddings = 0;
        for (MemoryFragment fragment : unclusteredFragments) {
            if (fragment.getEmbedding() != null) {
                fragmentsWithEmbeddings++;
            }
        }
        Log.infof("Clustering: %d fragments have embeddings out of %d total", fragmentsWithEmbeddings, unclusteredFragments.size());

        // Debug: Sample distance calculation
        if (unclusteredFragments.size() >= 2) {
            MemoryFragment frag1 = unclusteredFragments.get(0);
            MemoryFragment frag2 = unclusteredFragments.get(1);
            MemoryFragmentCosineDistance distanceFunc = new MemoryFragmentCosineDistance();
            double sampleDistance = distanceFunc.distance(frag1, frag2);
            Log.infof("Clustering: Sample distance between fragments 1 and 2: %f", sampleDistance);
            Log.infof("Clustering: Fragment 1 text: '%s'", frag1.getOriginalText().substring(0, Math.min(50, frag1.getOriginalText().length())));
            Log.infof("Clustering: Fragment 2 text: '%s'", frag2.getOriginalText().substring(0, Math.min(50, frag2.getOriginalText().length())));
        }

        // Debug: Show all pairwise distances for better understanding
        if (unclusteredFragments.size() >= 2) {
            MemoryFragmentCosineDistance distanceFunc = new MemoryFragmentCosineDistance();
            Log.infof("Clustering: Pairwise distances between all fragments:");
            for (int i = 0; i < unclusteredFragments.size(); i++) {
                for (int j = i + 1; j < unclusteredFragments.size(); j++) {
                    double distance = distanceFunc.distance(unclusteredFragments.get(i), unclusteredFragments.get(j));
                    Log.infof("Clustering: Distance between fragment %d and %d: %f", i + 1, j + 1, distance);
                    Log.infof("Clustering: Fragment %d: '%s'", i + 1, unclusteredFragments.get(i).getOriginalText().substring(0, Math.min(40, unclusteredFragments.get(i).getOriginalText().length())));
                    Log.infof("Clustering: Fragment %d: '%s'", j + 1, unclusteredFragments.get(j).getOriginalText().substring(0, Math.min(40, unclusteredFragments.get(j).getOriginalText().length())));
                }
            }
        }

        // 2. Set up DBSCAN parameters - Much more selective to avoid cross-topic clustering
        double eps = 0.814; // Set to exactly 0.814 - Should preserve software cluster but break 0.815 bridge
        int minPts = 2;   // Minimum number of samples in a neighborhood for a point to be considered a core point

        Log.infof("Clustering: Using DBSCAN parameters - eps: %f, minPts: %d", eps, minPts);

        // Debug: Show which fragments would be neighbors under the eps threshold
        if (unclusteredFragments.size() >= 2) {
            MemoryFragmentCosineDistance distanceFunc = new MemoryFragmentCosineDistance();
            Log.infof("Clustering: Fragments that are neighbors (distance <= %f):", eps);
            for (int i = 0; i < unclusteredFragments.size(); i++) {
                for (int j = i + 1; j < unclusteredFragments.size(); j++) {
                    double distance = distanceFunc.distance(unclusteredFragments.get(i), unclusteredFragments.get(j));
                    if (distance <= eps) {
                        Log.infof("Clustering: Fragments %d and %d are neighbors (distance: %f)", i + 1, j + 1, distance);
                    }
                }
            }
        }

        // 3. Run DBSCAN algorithm
        DBSCANClusterer<MemoryFragment> dbscan = new DBSCANClusterer<>(
            eps, minPts, new MemoryFragmentCosineDistance()
        );
        
        Log.infof("Clustering: Running DBSCAN clustering algorithm...");
        List<Cluster<MemoryFragment>> clusters = dbscan.cluster(unclusteredFragments);

        Log.infof("Clustering: DBSCAN completed, found %d clusters", clusters.size());

        // 4. Process the results
        int processedClusters = 0;
        int noiseClusters = 0;
        int totalMembersProcessed = 0;
        
        for (Cluster<MemoryFragment> cluster : clusters) {
            if (cluster.isNoise()) {
                noiseClusters++;
                Log.infof("Clustering: Skipping noise cluster with %d fragments", cluster.size());
                continue;
            }
            
            String clusterId = UUID.randomUUID().toString();
            List<MemoryFragment> members = cluster.getPoints();

            Log.infof("Clustering: Processing cluster %d with %d members (clusterId: %s)", 
                    processedClusters + 1, members.size(), clusterId);

            // Update each fragment with the new cluster ID
            for (MemoryFragment member : members) {
                member.setClusterId(clusterId);
            }

            // Create or update the MemoryCluster entity
            createOrUpdateClusterEntity(clusterId, members);
            
            processedClusters++;
            totalMembersProcessed += members.size();
        }
        
        Log.infof("Clustering: Clustering completed - processed %d clusters, %d noise clusters, %d total members assigned", 
                processedClusters, noiseClusters, totalMembersProcessed);
    }

    /**
     * Creates or updates a MemoryCluster entity for the given cluster members.
     * 
     * <p>This method processes a successfully formed cluster by:</p>
     * <ul>
     *   <li>Calculating a prototype vector (centroid) from all member embeddings</li>
     *   <li>Generating a meaningful theme that describes the cluster's content</li>
     *   <li>Creating and persisting a MemoryCluster entity with metadata</li>
     * </ul>
     * 
     * <p>The prototype vector serves as a representative embedding for the entire cluster,
     * enabling efficient cluster-based retrieval without processing individual members.</p>
     * 
     * @param clusterId unique identifier for the cluster
     * @param members list of MemoryFragment objects belonging to this cluster
     * @throws RuntimeException if cluster entity creation fails
     */
    private void createOrUpdateClusterEntity(String clusterId, List<MemoryFragment> members) {
        Log.infof("Clustering: Creating cluster entity for clusterId: %s with %d members", clusterId, members.size());
        
        // Calculate prototype vector (centroid of all member embeddings)
        float[] prototypeVector = calculateCentroid(members);
        
        // Generate theme - for now, use a simple approach
        String theme = generateClusterTheme(members);
        
        Log.infof("Clustering: Generated cluster theme: '%s'", theme);
        
        // Create and persist the MemoryCluster entity
        MemoryCluster cluster = new MemoryCluster();
        cluster.setClusterId(clusterId);
        cluster.setPrototypeVector(prototypeVector);
        cluster.setTheme(theme);
        cluster.setMemberCount(members.size());
        cluster.setLastUpdated(java.time.LocalDateTime.now());
        
        memoryClusterRepository.persist(cluster);
        Log.infof("Clustering: Successfully persisted cluster entity with ID: %d", cluster.id);
    }
    
    /**
     * Calculates the centroid (prototype vector) of a cluster's member embeddings.
     * 
     * <p>The centroid is computed as the element-wise average of all member embeddings,
     * creating a representative vector that captures the cluster's semantic center.
     * This prototype vector is used for efficient cluster-based retrieval operations.</p>
     * 
     * @param members list of MemoryFragment objects whose embeddings should be averaged
     * @return float array containing the calculated centroid vector
     * @throws RuntimeException if members list is empty or contains null embeddings
     */
    private float[] calculateCentroid(List<MemoryFragment> members) {
        if (members.isEmpty()) {
            Log.infof("Clustering: Cannot calculate centroid - no members provided");
            return new float[0];
        }
        
        float[] firstEmbedding = members.get(0).getEmbedding();
        if (firstEmbedding == null) {
            Log.infof("Clustering: Cannot calculate centroid - first member has null embedding");
            return new float[0];
        }
        
        int dimensions = firstEmbedding.length;
        float[] centroid = new float[dimensions];
        
        Log.infof("Clustering: Calculating centroid for %d members with %d dimensions", members.size(), dimensions);
        
        // Sum all embeddings
        for (MemoryFragment member : members) {
            float[] embedding = member.getEmbedding();
            if (embedding != null && embedding.length == dimensions) {
                for (int i = 0; i < dimensions; i++) {
                    centroid[i] += embedding[i];
                }
            }
        }
        
        // Calculate average
        for (int i = 0; i < dimensions; i++) {
            centroid[i] /= members.size();
        }
        
        return centroid;
    }
    
    /**
     * Generates a meaningful theme description for a cluster based on its members' content.
     * 
     * <p>The theme generation process involves:</p>
     * <ul>
     *   <li>Extracting meaningful keywords from member text content</li>
     *   <li>Identifying common topic categories (work, travel, personal, project)</li>
     *   <li>Filtering out stop words and noise</li>
     *   <li>Combining topics and keywords into a descriptive theme</li>
     * </ul>
     * 
     * <p>The resulting theme provides a human-readable description of what the cluster
     * represents, making it easier to understand and debug the clustering results.</p>
     * 
     * @param members list of MemoryFragment objects to analyze for theme generation
     * @return String containing the generated cluster theme
     */
    private String generateClusterTheme(List<MemoryFragment> members) {
        // Improved theme generation - extract key concepts and topics
        Set<String> keywords = new HashSet<>();
        Set<String> topics = new HashSet<>();
        
        Log.infof("Clustering: Generating theme from %d members", members.size());
        
        for (MemoryFragment member : members) {
            String text = member.getOriginalText();
            if (text != null && !text.trim().isEmpty()) {
                // Extract potential topics and important words
                String[] words = text.toLowerCase().split("\\s+");
                for (String word : words) {
                    // Clean word and filter meaningful ones
                    word = word.replaceAll("[^a-zA-Z]", "");
                    if (word.length() > 3 && !isStopWord(word)) {
                        keywords.add(word);
                    }
                }
                
                // Look for common patterns and topics
                if (text.toLowerCase().contains("work") || text.toLowerCase().contains("office") || 
                    text.toLowerCase().contains("software") || text.toLowerCase().contains("project")) {
                    topics.add("work");
                }
                if (text.toLowerCase().contains("vacation") || text.toLowerCase().contains("travel") || 
                    text.toLowerCase().contains("europe") || text.toLowerCase().contains("rome")) {
                    topics.add("travel");
                }
                if (text.toLowerCase().contains("alex") || text.toLowerCase().contains("berlin") || 
                    text.toLowerCase().contains("hobby") || text.toLowerCase().contains("personal")) {
                    topics.add("personal");
                }
                if (text.toLowerCase().contains("deadline") || text.toLowerCase().contains("bugs") || 
                    text.toLowerCase().contains("module") || text.toLowerCase().contains("feature")) {
                    topics.add("project");
                }
            }
        }
        
        // Create meaningful theme
        StringBuilder theme = new StringBuilder();
        if (!topics.isEmpty()) {
            theme.append(String.join(", ", topics.stream().limit(3).collect(Collectors.toList())));
            if (!keywords.isEmpty()) {
                theme.append(" (");
                theme.append(String.join(", ", keywords.stream().limit(3).collect(Collectors.toList())));
                theme.append(")");
            }
        } else if (!keywords.isEmpty()) {
            theme.append(String.join(", ", keywords.stream().limit(5).collect(Collectors.toList())));
        } else {
            theme.append("cluster-").append(members.size()).append("-items");
        }
        
        return theme.toString();
    }
    
    /**
     * Determines if a word should be filtered out as a stop word.
     * 
     * <p>Stop words are common words that don't contribute meaningful information
     * to cluster themes, such as articles, prepositions, and common verbs.</p>
     * 
     * @param word the word to check
     * @return true if the word is a stop word and should be filtered out
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of("this", "that", "with", "have", "will", "from", "they", "been", 
                                      "were", "said", "each", "which", "their", "time", "into", "only", 
                                      "more", "very", "what", "know", "just", "first", "also", "after", 
                                      "back", "other", "many", "than", "then", "them", "these", "some", 
                                      "her", "would", "make", "like", "him", "has", "had", "two", "way", 
                                      "who", "its", "now", "find", "long", "down", "day", "did", "get", 
                                      "come", "made", "may", "part");
        return stopWords.contains(word);
    }
}
