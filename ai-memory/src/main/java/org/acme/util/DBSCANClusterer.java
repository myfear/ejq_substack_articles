package org.acme.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acme.entities.Cluster;
import org.acme.entities.MemoryFragment;

/**
 * Implementation of the DBSCAN (Density-Based Spatial Clustering of Applications with Noise) algorithm.
 * 
 * <p>DBSCAN is a density-based clustering algorithm that groups together points that are
 * closely packed while marking points in low-density regions as noise. This implementation
 * is specifically designed for clustering objects based on distance functions, making it
 * suitable for semantic clustering of memory fragments using embedding similarity.</p>
 * 
 * <p>Key algorithm characteristics:</p>
 * <ul>
 *   <li><strong>Density-Based:</strong> Forms clusters based on point density rather than distance from centroids</li>
 *   <li><strong>Noise Detection:</strong> Automatically identifies and isolates outlier points</li>
 *   <li><strong>Arbitrary Shapes:</strong> Can find clusters of any shape, not limited to spherical clusters</li>
 *   <li><strong>Parameter-Driven:</strong> Behavior controlled by epsilon (neighborhood radius) and minimum points</li>
 * </ul>
 * 
 * <p>Algorithm parameters:</p>
 * <ul>
 *   <li><strong>eps (ε):</strong> Maximum distance between two points to be considered neighbors</li>
 *   <li><strong>minPts:</strong> Minimum number of points required to form a dense region (cluster)</li>
 * </ul>
 * 
 * <p>Point classifications:</p>
 * <ul>
 *   <li><strong>Core Point:</strong> Has at least minPts neighbors within eps distance</li>
 *   <li><strong>Border Point:</strong> Within eps distance of a core point but not core itself</li>
 *   <li><strong>Noise Point:</strong> Neither core nor border point</li>
 * </ul>
 * 
 * @param <T> the type of objects to be clustered
 * @author AI Memory System
 * @since 1.0
 */
public class DBSCANClusterer<T> {

    /**
     * Maximum distance between two points to be considered neighbors (epsilon parameter).
     */
    private final double eps;
    
    /**
     * Minimum number of points required to form a dense region.
     */
    private final int minPts;
    
    /**
     * Function used to calculate distance between two points.
     */
    private final DistanceFunction<T> distanceFunction;

    /**
     * Creates a new DBSCAN clusterer with the specified parameters.
     * 
     * @param eps the maximum distance between two points to be considered neighbors
     * @param minPts the minimum number of points required to form a dense region
     * @param distanceFunction the function used to calculate distance between points
     */
    public DBSCANClusterer(double eps, int minPts, DistanceFunction<T> distanceFunction) {
        this.eps = eps;
        this.minPts = minPts;
        this.distanceFunction = distanceFunction;
    }

    /**
     * Clusters the given points using the DBSCAN algorithm.
     * 
     * <p>The algorithm works by:</p>
     * <ol>
     *   <li>Iterating through all unvisited points</li>
     *   <li>Finding neighbors within eps distance for each point</li>
     *   <li>If a point has >= minPts neighbors, starting a new cluster from it</li>
     *   <li>Expanding the cluster by recursively adding neighbor points</li>
     *   <li>Marking points that can't form clusters as noise</li>
     * </ol>
     * 
     * <p>The result includes both valid clusters and a noise cluster containing
     * all points that couldn't be assigned to any dense region.</p>
     * 
     * @param points List of points to cluster
     * @return List of clusters, including a noise cluster if any noise points exist
     */
    public List<Cluster<T>> cluster(List<T> points) {
        Map<T, PointStatus> pointStatus = new HashMap<>();
        List<Cluster<T>> clusters = new ArrayList<>();

        // Initialize all points as unvisited
        for (T point : points) {
            pointStatus.put(point, PointStatus.UNVISITED);
        }

        for (T point : points) {
            if (pointStatus.get(point) == PointStatus.UNVISITED) {
                pointStatus.put(point, PointStatus.VISITED);

                // Find neighbors
                List<T> neighbors = getNeighbors(point, points);

                if (neighbors.size() >= minPts) {
                    // Core point - create new cluster
                    Cluster<T> cluster = new Cluster<>();
                    expandCluster(point, neighbors, cluster, pointStatus, points);
                    clusters.add(cluster);
                } else {
                    // Mark as noise (will be reconsidered if becomes border point)
                    pointStatus.put(point, PointStatus.NOISE);
                }
            }
        }

        // Create noise cluster for remaining noise points
        Cluster<T> noiseCluster = new Cluster<>();
        for (Map.Entry<T, PointStatus> entry : pointStatus.entrySet()) {
            if (entry.getValue() == PointStatus.NOISE) {
                noiseCluster.addPoint(entry.getKey());
            }
        }

        if (!noiseCluster.isEmpty()) {
            noiseCluster.setNoise(true);
            clusters.add(noiseCluster);
        }

        return clusters;
    }

    /**
     * Expands a cluster by recursively adding density-reachable points.
     * 
     * <p>This method implements the core cluster expansion logic of DBSCAN:</p>
     * <ul>
     *   <li>Adds the starting point to the cluster</li>
     *   <li>Iterates through all neighbors of the point</li>
     *   <li>For unvisited neighbors, finds their neighbors and potentially expands further</li>
     *   <li>Adds border points (neighbors of core points) to the cluster</li>
     * </ul>
     * 
     * @param point the core point to start expansion from
     * @param neighbors the initial neighbors of the core point
     * @param cluster the cluster being expanded
     * @param pointStatus map tracking the status of all points
     * @param allPoints complete list of all points being clustered
     */
    private void expandCluster(T point, List<T> neighbors, Cluster<T> cluster,
            Map<T, PointStatus> pointStatus, List<T> allPoints) {
        cluster.addPoint(point);
        pointStatus.put(point, PointStatus.CLUSTERED);

        int i = 0;
        while (i < neighbors.size()) {
            T neighbor = neighbors.get(i);
            PointStatus status = pointStatus.get(neighbor);

            if (status == PointStatus.UNVISITED) {
                pointStatus.put(neighbor, PointStatus.VISITED);
                List<T> neighborNeighbors = getNeighbors(neighbor, allPoints);

                if (neighborNeighbors.size() >= minPts) {
                    // Neighbor is also a core point, merge its neighbors
                    for (T nn : neighborNeighbors) {
                        if (!neighbors.contains(nn)) {
                            neighbors.add(nn);
                        }
                    }
                }
            }

            if (status != PointStatus.CLUSTERED) {
                cluster.addPoint(neighbor);
                pointStatus.put(neighbor, PointStatus.CLUSTERED);
            }

            i++;
        }
    }

    /**
     * Finds all neighbors of a point within the eps distance.
     * 
     * <p>This method identifies the neighborhood of a point by calculating
     * distances to all other points and selecting those within the eps threshold.
     * The neighborhood determination is crucial for identifying core points and
     * cluster boundaries.</p>
     * 
     * @param point the point to find neighbors for
     * @param allPoints complete list of all points
     * @return List of neighbors within eps distance
     */
    private List<T> getNeighbors(T point, List<T> allPoints) {
        List<T> neighbors = new ArrayList<>();

        for (T other : allPoints) {
            if (!point.equals(other) && distanceFunction.distance(point, other) <= eps) {
                neighbors.add(other);
            }
        }

        return neighbors;
    }

    /**
     * Enumeration representing the possible states of a point during DBSCAN clustering.
     * 
     * <p>Point status tracking is essential for the DBSCAN algorithm to avoid
     * reprocessing points and to correctly classify points as core, border, or noise.</p>
     */
    private enum PointStatus {
        /** Point has not been visited yet by the algorithm. */
        UNVISITED,
        
        /** Point has been visited but not yet assigned to a cluster. */
        VISITED,
        
        /** Point has been assigned to a cluster. */
        CLUSTERED,
        
        /** Point has been classified as noise. */
        NOISE
    }

    /**
     * Functional interface for calculating distance between two points.
     * 
     * <p>This interface allows the DBSCAN algorithm to work with any type of object
     * by providing custom distance calculation logic. Different distance functions
     * can be used for different clustering needs (Euclidean, cosine, etc.).</p>
     * 
     * @param <T> the type of objects to calculate distance between
     */
    @FunctionalInterface
    public interface DistanceFunction<T> {
        /**
         * Calculates the distance between two points.
         * 
         * @param a the first point
         * @param b the second point
         * @return the distance between the points (should be >= 0)
         */
        double distance(T a, T b);
    }

    /**
     * Euclidean distance function for MemoryFragment objects.
     * 
     * <p>This implementation calculates the Euclidean distance between two memory
     * fragments based on their embedding vectors. Euclidean distance treats
     * embedding dimensions equally and works well for many embedding models.</p>
     * 
     * <p>The distance is calculated as: √(Σ(ai - bi)²) where ai and bi are
     * corresponding elements of the embedding vectors.</p>
     */
    public static class MemoryFragmentDistance implements DistanceFunction<MemoryFragment> {
        
        /**
         * Calculates Euclidean distance between two memory fragments.
         * 
         * @param a the first memory fragment
         * @param b the second memory fragment
         * @return the Euclidean distance between their embedding vectors
         * @throws IllegalArgumentException if embeddings are null or have different dimensions
         */
        @Override
        public double distance(MemoryFragment a, MemoryFragment b) {
            float[] embeddingA = a.getEmbedding();
            float[] embeddingB = b.getEmbedding();

            if (embeddingA == null || embeddingB == null) {
                return Double.MAX_VALUE; // Treat null embeddings as infinitely distant
            }

            if (embeddingA.length != embeddingB.length) {
                throw new IllegalArgumentException("Embedding dimensions must match");
            }

            double sum = 0.0;
            for (int i = 0; i < embeddingA.length; i++) {
                double diff = embeddingA[i] - embeddingB[i];
                sum += diff * diff;
            }

            return Math.sqrt(sum);
        }
    }

    /**
     * Cosine distance function for MemoryFragment objects.
     * 
     * <p>This implementation calculates the cosine distance between two memory
     * fragments based on their embedding vectors. Cosine distance measures the
     * angle between vectors rather than their magnitude, making it particularly
     * suitable for high-dimensional embeddings where direction matters more than magnitude.</p>
     * 
     * <p>Cosine distance is calculated as: 1 - cosine_similarity, where cosine
     * similarity is the dot product divided by the product of vector magnitudes.</p>
     * 
     * <p>This distance function is preferred for semantic similarity as it focuses
     * on the orientation of embedding vectors rather than their absolute values.</p>
     */
    public static class MemoryFragmentCosineDistance implements DistanceFunction<MemoryFragment> {
        
        /**
         * Calculates cosine distance between two memory fragments.
         * 
         * @param a the first memory fragment
         * @param b the second memory fragment
         * @return the cosine distance between their embedding vectors (0 = identical, 2 = opposite)
         * @throws IllegalArgumentException if embeddings are null or have different dimensions
         */
        @Override
        public double distance(MemoryFragment a, MemoryFragment b) {
            float[] embeddingA = a.getEmbedding();
            float[] embeddingB = b.getEmbedding();

            if (embeddingA == null || embeddingB == null) {
                return 2.0; // Maximum cosine distance for null embeddings
            }

            if (embeddingA.length != embeddingB.length) {
                throw new IllegalArgumentException("Embedding dimensions must match");
            }

            double dotProduct = 0.0;
            double normA = 0.0;
            double normB = 0.0;

            for (int i = 0; i < embeddingA.length; i++) {
                dotProduct += embeddingA[i] * embeddingB[i];
                normA += embeddingA[i] * embeddingA[i];
                normB += embeddingB[i] * embeddingB[i];
            }

            if (normA == 0.0 || normB == 0.0) {
                return 2.0; // Maximum distance for zero vectors
            }

            double cosineSimilarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
            return 1.0 - cosineSimilarity; // Convert similarity to distance
        }
    }
}