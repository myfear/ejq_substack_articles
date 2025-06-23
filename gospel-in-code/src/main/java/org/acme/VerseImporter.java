package org.acme;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class VerseImporter {

    @Inject
    EntityManager em;

    @Inject
    EmbeddingModel embeddingModel;

    private static final int BATCH_SIZE = 500; // Larger batches for text-only import
    private static final int EMBEDDING_BATCH_SIZE = 50; // Batch size for embedding generation

    /**
     * Fast import of verses without embeddings from classpath resource
     */
    public void importFile(String resourcePath, String shortName) throws Exception {
        Log.info("Importing " + shortName + " from classpath: " + resourcePath);

        var inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new RuntimeException("Resource not found on classpath: " + resourcePath);
        }

        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

        String currentBook = null;
        int currentChapter = 0;
        Verse currentVerse = null;
        StringBuilder textBuilder = null;
        boolean captureText = false;
        List<Verse> verseBatch = new ArrayList<>();
        int totalProcessed = 0;

        try {
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        String elementName = reader.getLocalName();
                        if ("div".equals(elementName) && "book".equals(reader.getAttributeValue(null, "type"))) {
                            currentBook = reader.getAttributeValue(null, "osisID");
                        } else if ("chapter".equals(elementName)) {
                            String n = reader.getAttributeValue(null, "n");
                            if (n != null) {
                                try {
                                    currentChapter = Integer.parseInt(n);
                                } catch (NumberFormatException e) {
                                    currentChapter = 0;
                                }
                            }
                        } else if ("verse".equals(elementName) && reader.getAttributeValue(null, "sID") != null) {
                            // Start of a new verse (milestone style)
                            captureText = true;
                            textBuilder = new StringBuilder();
                            currentVerse = new Verse();
                            currentVerse.translation = shortName;
                            currentVerse.book = currentBook;
                            currentVerse.chapter = currentChapter;
                            String n = reader.getAttributeValue(null, "n");
                            if (n != null) {
                                try {
                                    currentVerse.verseNum = Integer.parseInt(n);
                                } catch (NumberFormatException e) {
                                    currentVerse.verseNum = 0;
                                }
                            }
                        } else if ("verse".equals(elementName) && reader.getAttributeValue(null, "eID") != null) {
                            // End of a verse (milestone style)
                            if (currentVerse != null && textBuilder != null
                                    && !textBuilder.toString().trim().isEmpty()) {
                                currentVerse.text = textBuilder.toString().trim();
                                verseBatch.add(currentVerse);
                            }
                            currentVerse = null;
                            textBuilder = null;
                            captureText = false;
                            if (verseBatch.size() >= BATCH_SIZE) {
                                importTextBatch(verseBatch);
                                totalProcessed += verseBatch.size();
                                verseBatch.clear();
                                if (totalProcessed % 5000 == 0) {
                                    Log.info("Imported " + totalProcessed + " verses so far...");
                                }
                            }
                        } else if ("note".equals(elementName)) {
                            captureText = false;
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if (captureText && textBuilder != null) {
                            textBuilder.append(reader.getText().trim()).append(" ");
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        String endElement = reader.getLocalName();
                        if ("note".equals(endElement)) {
                            if (currentVerse != null) {
                                captureText = true;
                            }
                        }
                        break;
                }
            }
            if (!verseBatch.isEmpty()) {
                importTextBatch(verseBatch);
                totalProcessed += verseBatch.size();
            }
        } finally {
            reader.close();
            inputStream.close();
        }
        Log.info("Finished importing " + shortName + " - total verses: " + totalProcessed);
    }

    /**
     * Fast batch import of text data only (no embeddings)
     */
    @Transactional
    public void importTextBatch(List<Verse> verses) {
        if (verses.isEmpty())
            return;

        for (Verse v : verses) {
            v.persist();
        }
    }

    /**
     * Generate embeddings for verses that don't have them yet
     */
    public void generateMissingEmbeddings() {
        Log.info("Starting embedding generation for verses without embeddings...");

        long totalToProcess = Verse.count("embedding IS NULL");
        Log.info("Found " + totalToProcess + " verses without embeddings");

        if (totalToProcess == 0) {
            Log.info("All verses already have embeddings!");
            return;
        }

        int processed = 0;
        int batchNum = 0;

        while (true) {
            List<Verse> batch = Verse.find("embedding IS NULL")
                    .range(0, EMBEDDING_BATCH_SIZE - 1)
                    .list();

            Log.debugf("Batch %d: batchSize=%d, verseIds=%s", batchNum, batch.size(),
                    batch.stream().map(v -> v.id).toList());

            if (batch.isEmpty()) {
                Log.warnf("Batch %d is empty. Breaking.", batchNum);
                break;
            }

            generateEmbeddingsBatch(batch);
            processed += batch.size();
            batchNum++;

            if (processed % 500 == 0) {
                Log.debugf("Generated embeddings for " + processed + "/" + totalToProcess + " verses");
            }
        }

        Log.info("Finished generating embeddings for " + processed + " verses");
    }

    /**
     * Generate embeddings for a specific translation
     */
    public void generateEmbeddingsForTranslation(String translation) {
        Log.info("Starting embedding generation for translation: " + translation);

        long totalToProcess = Verse.count("translation = ?1 AND embedding IS NULL", translation);
        Log.info("Found " + totalToProcess + " verses without embeddings for " + translation);

        if (totalToProcess == 0) {
            Log.info("All verses for " + translation + " already have embeddings!");
            return;
        }

        int offset = 0;
        int processed = 0;

        while (offset < totalToProcess) {
            List<Verse> batch = Verse.find("translation = ?1 AND embedding IS NULL", translation)
                    .page(offset / EMBEDDING_BATCH_SIZE, EMBEDDING_BATCH_SIZE)
                    .list();

            if (batch.isEmpty())
                break;

            generateEmbeddingsBatch(batch);
            processed += batch.size();
            offset += EMBEDDING_BATCH_SIZE;

            if (processed % 100 == 0) {
                Log.info(
                        "Generated embeddings for " + processed + "/" + totalToProcess + " verses in " + translation);
            }
        }

        Log.info("Finished generating embeddings for " + processed + " verses in " + translation);
    }

    /**
     * Generate embeddings for a batch of verses and update them in the database
     */
    // No @Transactional here!
    public void generateEmbeddingsBatch(List<Verse> verses) {
        if (verses.isEmpty())
            return;
        // First, generate all embeddings outside of a transaction
        for (Verse v : verses) {
            try {
                Log.debugf("Generating embedding for verseId=%s, translation=%s, book=%s, chapter=%d, verseNum=%d",
                        v.id, v.translation, v.book, v.chapter, v.verseNum);
                float[] embedding = embeddingModel.embed(v.text).content().vector();

                v.setEmbedding(embedding);
            } catch (Exception e) {
                Log.error("Failed to generate embedding for verse: " +
                        v.translation + " " + v.book + " " + v.chapter + ":" + v.verseNum + " (id=" + v.id + ")");
                Log.error("Error: " + e.getMessage());
            }
        }
        // Now persist all verses in a transaction
        persistVersesWithEmbeddings(verses);
    }

    @Transactional
    protected void persistVersesWithEmbeddings(List<Verse> verses) {
        // Explicitly merge each verse to ensure they are updated in the database
        verses.forEach(em::merge);
    }

    /**
     * Get statistics about embedding generation progress
     */
    public EmbeddingStats getEmbeddingStats() {
        long totalVerses = Verse.count();
        long versesWithEmbeddings = Verse.count("embedding IS NOT NULL");
        long versesWithoutEmbeddings = totalVerses - versesWithEmbeddings;

        return new EmbeddingStats(totalVerses, versesWithEmbeddings, versesWithoutEmbeddings);
    }

    public static class EmbeddingStats {
        public final long totalVerses;
        public final long versesWithEmbeddings;
        public final long versesWithoutEmbeddings;

        public EmbeddingStats(long totalVerses, long versesWithEmbeddings, long versesWithoutEmbeddings) {
            this.totalVerses = totalVerses;
            this.versesWithEmbeddings = versesWithEmbeddings;
            this.versesWithoutEmbeddings = versesWithoutEmbeddings;
        }

        public double getCompletionPercentage() {
            if (totalVerses == 0)
                return 0.0;
            return (versesWithEmbeddings * 100.0) / totalVerses;
        }

        @Override
        public String toString() {
            return String.format(
                    "EmbeddingStats{totalVerses=%d, versesWithEmbeddings=%d, versesWithoutEmbeddings=%d, completion=%.2f%%}",
                    totalVerses, versesWithEmbeddings, versesWithoutEmbeddings, getCompletionPercentage());
        }

    }
}