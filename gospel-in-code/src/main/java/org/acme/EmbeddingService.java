package org.acme;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.logging.Logger;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class EmbeddingService {

    private static final Logger LOG = Logger.getLogger(EmbeddingService.class);

    @Inject
    EntityManager em;

    @Inject
    EmbeddingModel embeddingModel;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicLong processedCount = new AtomicLong(0);
    private final AtomicLong totalCount = new AtomicLong(0);

    private static final int BATCH_SIZE = 25; // Smaller batches for embedding generation
    private static final int MAX_RETRIES = 3;

    /**
     * Start embedding generation in the background (Mutiny reactive)
     */
    public Uni<Void> startBackgroundEmbeddingGeneration() {
        if (isRunning.get()) {
            return Uni.createFrom().failure(new IllegalStateException("Embedding generation is already running"));
        }
        return Uni.createFrom().voidItem()
            .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
            .invoke(() -> {
                isRunning.set(true);
                processedCount.set(0);
                try {
                    generateAllMissingEmbeddings();
                } finally {
                    isRunning.set(false);
                }
            });
    }

    /**
     * Generate embeddings for all verses that don't have them
     */
    private void generateAllMissingEmbeddings() {
        totalCount.set(Verse.count("embedding IS NULL"));
        LOG.info("Starting embedding generation for " + totalCount.get() + " verses");
        
        if (totalCount.get() == 0) {
            LOG.info("All verses already have embeddings!");
            return;
        }

        while (isRunning.get()) {
            List<Verse> batch = Verse.find("embedding IS NULL")
                .range(0, BATCH_SIZE - 1)
                .list();
            
            if (batch.isEmpty()) {
                long stillMissing = Verse.count("embedding IS NULL");
                if (stillMissing > 0) {
                    LOG.error("WARNING: No batch returned but " + stillMissing + " verses still missing embeddings. Likely stuck on problematic verses.");
                }
                break;
            }
            
            processBatchWithRetry(batch);
            em.clear(); // Clear persistence context to avoid stale results
            processedCount.addAndGet(batch.size());

            // Log progress every 100 verses
            if (processedCount.get() % 100 == 0) {
                double percentage = (processedCount.get() * 100.0) / totalCount.get();
                LOG.infof("Embedding progress: %d/%d (%.1f%%)", 
                    processedCount.get(), totalCount.get(), percentage);
            }

            // Small delay to prevent overwhelming the embedding service
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        long stillMissing = Verse.count("embedding IS NULL");
        LOG.info("Embedding generation completed. Processed: " + processedCount.get() + " verses. Still missing: " + stillMissing);
    }

    /**
     * Process a batch with retry logic
     */
    private void processBatchWithRetry(List<Verse> batch) {
        int attempts = 0;
        boolean batchFailed = false;
        while (attempts < MAX_RETRIES) {
            try {
                processBatch(batch);
                return; // Success, exit retry loop
            } catch (Exception e) {
                attempts++;
                LOG.error("Batch processing failed (attempt " + attempts + "/" + MAX_RETRIES + "): " + e.getMessage());
                batchFailed = true;
                if (attempts >= MAX_RETRIES) {
                    LOG.error("Max retries reached for batch. Processing individual verses...");
                    processIndividualVerses(batch);
                } else {
                    // Wait before retry
                    try {
                        Thread.sleep(1000 * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
        if (batchFailed) {
            LOG.error("Batch permanently failed after max retries. See above for details.");
        }
    }

    /**
     * Process individual verses when batch processing fails
     */
    private void processIndividualVerses(List<Verse> batch) {
        for (Verse verse : batch) {
            try {
                processIndividualVerse(verse);
            } catch (Exception e) {
                LOG.error("Failed to process individual verse: " + 
                    verse.translation + " " + verse.book + " " + verse.chapter + ":" + verse.verseNum +
                    " - " + e.getMessage());
            }
        }
    }

    /**
     * Process a single verse
     */
    @Transactional
    public void processIndividualVerse(Verse verse) {
        try {
            float[] embedding = embeddingModel.embed(verse.text).content().vector();

            verse.setEmbedding(embedding) ;
            verse.persist();
        } catch (Exception e) {
            LOG.error("Failed to generate embedding for verse: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Process a batch of verses
     */
    @Transactional
    public void processBatch(List<Verse> verses) {
        for (Verse verse : verses) {
            try {
                LOG.info("Processing verse: " + verse.translation + " " + verse.book + " " + verse.chapter + ":" + verse.verseNum);
                float[] embedding = embeddingModel.embed(verse.text).content().vector();

                verse.setEmbedding (embedding) ;
                verse.persist();
            } catch (Exception e) {
                LOG.error("Failed to generate embedding for verse in batch: " +
                    verse.translation + " " + verse.book + " " + verse.chapter + ":" + verse.verseNum +
                    " - " + e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Get current progress information
     */
    public EmbeddingProgress getProgress() {
        return new EmbeddingProgress(
            isRunning.get(),
            processedCount.get(),
            totalCount.get(),
            calculateCompletionPercentage()
        );
    }

    private double calculateCompletionPercentage() {
        if (totalCount.get() == 0) return 100.0;
        return (processedCount.get() * 100.0) / totalCount.get();
    }

    /**
     * Stop the embedding generation process
     */
    public void stop() {
        isRunning.set(false);
    }

    /**
     * Check if embedding generation is currently running
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    public static class EmbeddingProgress {
        public final boolean isRunning;
        public final long processed;
        public final long total;
        public final double completionPercentage;

        public EmbeddingProgress(boolean isRunning, long processed, long total, double completionPercentage) {
            this.isRunning = isRunning;
            this.processed = processed;
            this.total = total;
            this.completionPercentage = completionPercentage;
        }
    }
}