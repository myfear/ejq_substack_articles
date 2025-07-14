package org.acme.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic cluster data structure for DBSCAN clustering algorithm results.
 * 
 * <p>This class represents a cluster of objects produced by the DBSCAN clustering
 * algorithm. It can hold any type of object that implements clustering requirements
 * and provides mechanisms to distinguish between valid clusters and noise.</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li><strong>Generic Type Support:</strong> Can cluster any type of object</li>
 *   <li><strong>Noise Detection:</strong> Distinguishes between valid clusters and noise points</li>
 *   <li><strong>Dynamic Membership:</strong> Supports adding points to clusters during formation</li>
 *   <li><strong>Size Information:</strong> Provides convenient access to cluster size</li>
 * </ul>
 * 
 * <p>In the DBSCAN algorithm context:</p>
 * <ul>
 *   <li><strong>Valid Clusters:</strong> Groups of points that meet density requirements</li>
 *   <li><strong>Noise Clusters:</strong> Collections of points that don't form dense regions</li>
 * </ul>
 * 
 * <p>This class is primarily used as an intermediate data structure during the
 * clustering process before results are converted to persistent entities like
 * {@link MemoryCluster}.</p>
 * 
 * @param <T> the type of objects being clustered
 * @author AI Memory System
 * @since 1.0
 */
public class Cluster<T> {
    
    /**
     * List of objects that belong to this cluster.
     * 
     * <p>Contains all the points that have been assigned to this cluster
     * during the DBSCAN algorithm execution. For valid clusters, these
     * points meet the density requirements. For noise clusters, these
     * are points that couldn't be assigned to any valid cluster.</p>
     */
    private List<T> points;
    
    /**
     * Flag indicating whether this cluster represents noise.
     * 
     * <p>In DBSCAN, points that don't belong to any dense region are
     * classified as noise. This flag distinguishes between:
     * <ul>
     *   <li><strong>false:</strong> Valid cluster with sufficient density</li>
     *   <li><strong>true:</strong> Noise cluster containing outlier points</li>
     * </ul>
     */
    private boolean isNoise;
    
    /**
     * Creates an empty cluster with no initial points.
     * 
     * <p>The cluster is initialized as a valid cluster (not noise).
     * Points can be added later using {@link #addPoint(Object)}.</p>
     */
    public Cluster() {
        this.points = new ArrayList<>();
        this.isNoise = false;
    }
    
    /**
     * Creates a cluster with the specified initial points.
     * 
     * <p>The cluster is initialized as a valid cluster (not noise) with
     * a copy of the provided points list.</p>
     * 
     * @param points the initial points to include in this cluster
     */
    public Cluster(List<T> points) {
        this.points = new ArrayList<>(points);
        this.isNoise = false;
    }
    
    /**
     * Adds a point to this cluster.
     * 
     * <p>This method is typically called during the cluster formation
     * process when the DBSCAN algorithm assigns points to clusters.</p>
     * 
     * @param point the point to add to this cluster
     */
    public void addPoint(T point) {
        this.points.add(point);
    }
    
    /**
     * Gets a copy of all points in this cluster.
     * 
     * <p>Returns a new list containing all points to prevent external
     * modification of the cluster's internal state. Use {@link #addPoint(Object)}
     * to add new points to the cluster.</p>
     * 
     * @return a new list containing all points in this cluster
     */
    public List<T> getPoints() {
        return new ArrayList<>(points);
    }
    
    /**
     * Gets the number of points in this cluster.
     * 
     * @return the cluster size (number of points)
     */
    public int size() {
        return points.size();
    }
    
    /**
     * Checks if this cluster is empty.
     * 
     * @return true if the cluster contains no points, false otherwise
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }
    
    /**
     * Checks if this cluster represents noise.
     * 
     * <p>Noise clusters contain points that couldn't be assigned to any
     * valid dense cluster during the DBSCAN algorithm execution.</p>
     * 
     * @return true if this cluster represents noise, false for valid clusters
     */
    public boolean isNoise() {
        return isNoise;
    }
    
    /**
     * Sets the noise status of this cluster.
     * 
     * <p>This method is typically called by the DBSCAN algorithm to mark
     * clusters as noise when they don't meet density requirements.</p>
     * 
     * @param noise true to mark as noise cluster, false for valid cluster
     */
    public void setNoise(boolean noise) {
        this.isNoise = noise;
    }
    
    /**
     * Returns a string representation of this cluster.
     * 
     * <p>Includes the number of points and noise status for debugging
     * and logging purposes.</p>
     * 
     * @return string representation of the cluster
     */
    @Override
    public String toString() {
        return "Cluster{" +
                "points=" + points.size() +
                ", isNoise=" + isNoise +
                '}';
    }
} 