package com.example.ai.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe statistics tracker for semantic cache operations.
 * Provides metrics on cache hits, misses, and performance characteristics.
 */
public class CacheStatistics {

    private final AtomicLong totalHits = new AtomicLong(0);
    private final AtomicLong totalMisses = new AtomicLong(0);
    private final AtomicLong exactMatchHits = new AtomicLong(0);
    private final AtomicLong semanticMatchHits = new AtomicLong(0);
    private final AtomicLong totalEntries = new AtomicLong(0);
    private final AtomicLong totalLookupTimeMs = new AtomicLong(0);
    private final AtomicLong totalStoreTimeMs = new AtomicLong(0);
    private final Instant startTime;

    public CacheStatistics() {
        this.startTime = Instant.now();
    }

    // ========================================================================
    // Recording Methods
    // ========================================================================

    /**
     * Record a cache hit
     * 
     * @param exactMatch true if it was an exact match, false if semantic match
     */
    public void recordHit(boolean exactMatch) {
        totalHits.incrementAndGet();
        if (exactMatch) {
            exactMatchHits.incrementAndGet();
        } else {
            semanticMatchHits.incrementAndGet();
        }
    }

    /**
     * Record a cache miss
     */
    public void recordMiss() {
        totalMisses.incrementAndGet();
    }

    /**
     * Record lookup execution time
     * 
     * @param durationMs duration in milliseconds
     */
    public void recordLookupTime(long durationMs) {
        totalLookupTimeMs.addAndGet(durationMs);
    }

    /**
     * Record store execution time
     * 
     * @param durationMs duration in milliseconds
     */
    public void recordStoreTime(long durationMs) {
        totalStoreTimeMs.addAndGet(durationMs);
    }

    /**
     * Update total entry count
     * 
     * @param count current number of entries in cache
     */
    public void updateEntryCount(long count) {
        totalEntries.set(count);
    }

    /**
     * Increment entry count by 1
     */
    public void incrementEntryCount() {
        totalEntries.incrementAndGet();
    }

    /**
     * Decrement entry count by 1
     */
    public void decrementEntryCount() {
        totalEntries.decrementAndGet();
    }

    // ========================================================================
    // Getter Methods
    // ========================================================================

    public long getTotalHits() {
        return totalHits.get();
    }

    public long getTotalMisses() {
        return totalMisses.get();
    }

    public long getExactMatchHits() {
        return exactMatchHits.get();
    }

    public long getSemanticMatchHits() {
        return semanticMatchHits.get();
    }

    public long getTotalEntries() {
        return totalEntries.get();
    }

    public long getTotalRequests() {
        return totalHits.get() + totalMisses.get();
    }

    public long getAverageLookupTimeMs() {
        long requests = getTotalRequests();
        return requests == 0 ? 0 : totalLookupTimeMs.get() / requests;
    }

    public long getAverageStoreTimeMs() {
        long hits = totalMisses.get(); // Stores happen on misses
        return hits == 0 ? 0 : totalStoreTimeMs.get() / hits;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Duration getUptime() {
        return Duration.between(startTime, Instant.now());
    }

    // ========================================================================
    // Calculated Metrics
    // ========================================================================

    /**
     * Calculate overall cache hit rate
     * 
     * @return hit rate as a percentage (0.0 to 1.0)
     */
    public double getHitRate() {
        long total = getTotalRequests();
        return total == 0 ? 0.0 : (double) totalHits.get() / total;
    }

    /**
     * Calculate hit rate as percentage
     * 
     * @return hit rate as percentage (0 to 100)
     */
    public double getHitRatePercent() {
        return getHitRate() * 100.0;
    }

    /**
     * Calculate miss rate
     * 
     * @return miss rate as a percentage (0.0 to 1.0)
     */
    public double getMissRate() {
        return 1.0 - getHitRate();
    }

    /**
     * Calculate percentage of hits that were exact matches
     * 
     * @return percentage (0.0 to 1.0)
     */
    public double getExactMatchRate() {
        long hits = totalHits.get();
        return hits == 0 ? 0.0 : (double) exactMatchHits.get() / hits;
    }

    /**
     * Calculate percentage of hits that were semantic matches
     * 
     * @return percentage (0.0 to 1.0)
     */
    public double getSemanticMatchRate() {
        long hits = totalHits.get();
        return hits == 0 ? 0.0 : (double) semanticMatchHits.get() / hits;
    }

    /**
     * Calculate requests per second since cache start
     * 
     * @return average requests per second
     */
    public double getRequestsPerSecond() {
        long uptimeSeconds = getUptime().getSeconds();
        return uptimeSeconds == 0 ? 0.0 : (double) getTotalRequests() / uptimeSeconds;
    }

    // ========================================================================
    // Reset and Summary
    // ========================================================================

    /**
     * Reset all statistics to zero (keeps start time)
     */
    public void reset() {
        totalHits.set(0);
        totalMisses.set(0);
        exactMatchHits.set(0);
        semanticMatchHits.set(0);
        totalLookupTimeMs.set(0);
        totalStoreTimeMs.set(0);
        // Note: totalEntries and startTime are not reset
    }

    /**
     * Get a snapshot of current statistics
     * 
     * @return immutable snapshot
     */
    public StatisticsSnapshot getSnapshot() {
        return new StatisticsSnapshot(
                totalHits.get(),
                totalMisses.get(),
                exactMatchHits.get(),
                semanticMatchHits.get(),
                totalEntries.get(),
                getHitRate(),
                getAverageLookupTimeMs(),
                getAverageStoreTimeMs(),
                startTime,
                getUptime());
    }

    /**
     * Generate a human-readable summary
     */
    @Override
    public String toString() {
        return String.format(
                "CacheStatistics{requests=%d, hits=%d (%.1f%%), misses=%d, " +
                        "exactMatches=%d, semanticMatches=%d, entries=%d, " +
                        "avgLookupMs=%d, avgStoreMs=%d, uptime=%s}",
                getTotalRequests(),
                totalHits.get(),
                getHitRatePercent(),
                totalMisses.get(),
                exactMatchHits.get(),
                semanticMatchHits.get(),
                totalEntries.get(),
                getAverageLookupTimeMs(),
                getAverageStoreTimeMs(),
                formatDuration(getUptime()));
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    // ========================================================================
    // Immutable Snapshot Class
    // ========================================================================

    /**
     * Immutable snapshot of cache statistics at a point in time
     */
    public static class StatisticsSnapshot {
        private final long totalHits;
        private final long totalMisses;
        private final long exactMatchHits;
        private final long semanticMatchHits;
        private final long totalEntries;
        private final double hitRate;
        private final long averageLookupTimeMs;
        private final long averageStoreTimeMs;
        private final Instant startTime;
        private final Duration uptime;

        public StatisticsSnapshot(
                long totalHits,
                long totalMisses,
                long exactMatchHits,
                long semanticMatchHits,
                long totalEntries,
                double hitRate,
                long averageLookupTimeMs,
                long averageStoreTimeMs,
                Instant startTime,
                Duration uptime) {
            this.totalHits = totalHits;
            this.totalMisses = totalMisses;
            this.exactMatchHits = exactMatchHits;
            this.semanticMatchHits = semanticMatchHits;
            this.totalEntries = totalEntries;
            this.hitRate = hitRate;
            this.averageLookupTimeMs = averageLookupTimeMs;
            this.averageStoreTimeMs = averageStoreTimeMs;
            this.startTime = startTime;
            this.uptime = uptime;
        }

        // Getters
        public long getTotalHits() {
            return totalHits;
        }

        public long getTotalMisses() {
            return totalMisses;
        }

        public long getExactMatchHits() {
            return exactMatchHits;
        }

        public long getSemanticMatchHits() {
            return semanticMatchHits;
        }

        public long getTotalEntries() {
            return totalEntries;
        }

        public double getHitRate() {
            return hitRate;
        }

        public long getAverageLookupTimeMs() {
            return averageLookupTimeMs;
        }

        public long getAverageStoreTimeMs() {
            return averageStoreTimeMs;
        }

        public Instant getStartTime() {
            return startTime;
        }

        public Duration getUptime() {
            return uptime;
        }

        public long getTotalRequests() {
            return totalHits + totalMisses;
        }

        @Override
        public String toString() {
            return String.format(
                    "StatisticsSnapshot{requests=%d, hits=%d (%.1f%%), misses=%d, " +
                            "exactMatches=%d, semanticMatches=%d, entries=%d}",
                    getTotalRequests(),
                    totalHits,
                    hitRate * 100.0,
                    totalMisses,
                    exactMatchHits,
                    semanticMatchHits,
                    totalEntries);
        }
    }
}