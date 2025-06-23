package org.acme;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/import")
public class ImportResource {

    @Inject
    VerseImporter importer;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GET
    @Path("/text")
    @Produces(MediaType.TEXT_PLAIN)
    public String importText() {
        if (getVerseCount() > 0) {
            return "Database already contains data. Skipping import.";
        }

        // Run text import asynchronously
        CompletableFuture.runAsync(() -> importTextAsync(), executorService);

        return "Text import started asynchronously. Check server logs for progress. Use /import/status to check progress.";
    }

    @ActivateRequestContext
    @Transactional
    public void importTextAsync() {
        try {
            System.out.println("Starting async text import process...");

            // Import files from classpath - text only, very fast
            importer.importFile("/data/eng-kjv.osis.xml", "KJV");
            importer.importFile("/data/eng-us-oeb.osis.xml", "OEB");
            importer.importFile("/data/eng-gb-oeb.osis.xml", "OCW");

            Log.info("Text import completed successfully. Total verses: " + Verse.count());
        } catch (Exception e) {
            Log.error("Error during text import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GET
    @Path("/embeddings")
    @Produces(MediaType.TEXT_PLAIN)
    public String generateEmbeddings() {
        long versesWithoutEmbeddings = getVersesWithoutEmbeddingsCount();

        if (versesWithoutEmbeddings == 0) {
            return "All verses already have embeddings!";
        }

        // Run embedding generation asynchronously
        CompletableFuture.runAsync(() -> generateEmbeddingsAsync(), executorService);

        return "Embedding generation started asynchronously for " + versesWithoutEmbeddings +
                " verses. Check server logs for progress. Use /import/status to check progress.";
    }

    @ActivateRequestContext
    public void generateEmbeddingsAsync() {
        try {
            Log.info("Starting async embedding generation process...");
            importer.generateMissingEmbeddings();
            Log.info("Embedding generation completed successfully.");
        } catch (Exception e) {
            Log.error("Error during embedding generation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GET
    @Path("/embeddings/{translation}")
    @Produces(MediaType.TEXT_PLAIN)
    public String generateEmbeddingsForTranslation(@PathParam("translation") String translation) {
        long versesWithoutEmbeddings = getVersesWithoutEmbeddingsForTranslation(translation);

        if (versesWithoutEmbeddings == 0) {
            return "All verses for translation '" + translation + "' already have embeddings!";
        }

        // Run embedding generation for specific translation asynchronously
        CompletableFuture.runAsync(() -> generateEmbeddingsForTranslationAsync(translation), executorService);

        return "Embedding generation started asynchronously for " + versesWithoutEmbeddings +
                " verses in translation '" + translation + "'. Check server logs for progress.";
    }

    @ActivateRequestContext
    public void generateEmbeddingsForTranslationAsync(String translation) {
        try {
            Log.info("Starting async embedding generation for translation: " + translation);
            importer.generateEmbeddingsForTranslation(translation);
            Log.info("Embedding generation for " + translation + " completed successfully.");
        } catch (Exception e) {
            Log.error("Error during embedding generation for " + translation + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public VerseImporter.EmbeddingStats getStatus() {
        return importer.getEmbeddingStats();
    }

    @GET
    @Path("/full")
    @Produces(MediaType.TEXT_PLAIN)
    public String doFullImport() {
        if (getVerseCount() > 0) {
            return "Database already contains data. Use /import/text and /import/embeddings separately, or clear the database first.";
        }

        // Run full import asynchronously
        CompletableFuture.runAsync(() -> doFullImportAsync(), executorService);

        return "Full import (text + embeddings) started asynchronously. This will take a while. Check server logs for progress.";
    }

    @ActivateRequestContext
    @Transactional
    public void doFullImportAsync() {
        try {
            Log.info("Starting full import process (text + embeddings)...");

            // First, import text data from classpath
            importer.importFile("/data/eng-kjv.osis.xml", "KJV");
            importer.importFile("/data/eng-web.osis.xml", "WEB");

            Log.info("Text import completed. Starting embedding generation...");

            // Then generate embeddings
            importer.generateMissingEmbeddings();

            Log.info("Full import completed successfully. Total verses: " + Verse.count());
        } catch (Exception e) {
            Log.error("Error during full import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper methods to get counts within proper context
    @Transactional
    public long getVerseCount() {
        return Verse.count();
    }

    @Transactional
    public long getVersesWithoutEmbeddingsCount() {
        return Verse.count("embedding IS NULL");
    }

    @Transactional
    public long getVersesWithoutEmbeddingsForTranslation(String translation) {
        return Verse.count("translation = ?1 AND embedding IS NULL", translation);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String showHelp() {
        return """
                Available endpoints:
                - GET /import/text - Import text data only (fast)
                - GET /import/embeddings - Generate embeddings for all verses without them
                - GET /import/embeddings/{translation} - Generate embeddings for specific translation
                - GET /import/status - Check current import/embedding status
                - GET /import/full - Import text + generate embeddings (slow)
                - GET /import - Show this help
                """;
    }
}