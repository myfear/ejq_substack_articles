package com.ibm.ingest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@Startup
public class DocumentLoader {

    @Inject
    EmbeddingStore<TextSegment> store;

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    DoclingConverter doclingConverter;

    @PostConstruct
    void loadDocument() throws Exception {

        Log.infof("Starting document loading...");

        // Read the files from /resources/documents folder
        Path documentsPath = Path.of("src/main/resources/documents");

        // Collect all documents first
        List<Document> docs = new ArrayList<>();

        // For each file in the folder, convert the file to markdown with docling
        // converter
        if (Files.exists(documentsPath) && Files.isDirectory(documentsPath)) {
            int successCount = 0;
            int failureCount = 0;

            // Only process files with allowed extensions
            List<String> allowedExtensions = Arrays.asList("txt", "pdf", "pptx", "ppt", "doc", "docx", "xlsx", "xls",
                    "csv", "json", "xml", "html");

            int skippedCount = 0;
            try (var stream = Files.list(documentsPath)) {
                for (Path filePath : stream.filter(Files::isRegularFile).toList()) {
                    File file = filePath.toFile();
                    String fileName = file.getName();

                    // Extract file extension
                    int lastDotIndex = fileName.lastIndexOf('.');
                    String extension = (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1)
                            ? fileName.substring(lastDotIndex + 1).toLowerCase()
                            : "";

                    // Skip files that don't have an allowed extension
                    if (extension.isEmpty() || !allowedExtensions.contains(extension)) {
                        skippedCount++;
                        Log.debugf("Skipping file '%s' - extension '%s' is not in allowed list: %s",
                                fileName, extension.isEmpty() ? "(no extension)" : extension, allowedExtensions);
                        continue;
                    }

                    try {
                        Log.infof("Processing file: %s", file.getName());

                        String markdown = doclingConverter.toMarkdown(file);

                        // Add filename and format to the Document before splitting
                        // Text segments retain and propagate document-level metadata during splitting
                        Map<String, String> meta = new HashMap<>();
                        meta.put("file", file.getName());
                        meta.put("format", extension);

                        // Create a Document with metadata
                        Document doc = Document.document(markdown, new Metadata(meta));
                        docs.add(doc);
                        successCount++;
                        Log.infof("Successfully processed file: %s", file.getName());
                    } catch (Exception e) {
                        failureCount++;
                        Log.errorf(e, "Failed to process file: %s. Error: %s", filePath, e.getMessage());
                        // Continue processing other files instead of failing the entire startup
                    }
                }
            }

            Log.infof("Document loading completed. Success: %d, Failures: %d, Skipped: %d", successCount, failureCount,
                    skippedCount);

            if (docs.isEmpty()) {
                Log.warn("No documents were successfully loaded. Please check the logs for errors.");
            }
        }

        // Only process if we have documents
        if (docs.isEmpty()) {
            Log.warn("No documents to process. Skipping embedding generation.");
            return;
        }

        // Add context splitting with a BySentence Splitter
        DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(200, 20); // 200 tokens, overlap of 20
        List<TextSegment> segments = splitter.splitAll(docs);

        if (segments.isEmpty()) {
            Log.warn("No text segments generated from documents. Skipping embedding storage.");
            return;
        }

        Log.infof("Generating embeddings for %d text segments...", segments.size());

        // Test the store before processing all segments
        int embeddedCount = 0;
        try {
            // Try to add a test embedding to verify store is working
            if (!segments.isEmpty()) {
                TextSegment testSegment = segments.get(0);
                var testEmbedding = embeddingModel.embed(testSegment).content();
                store.add(testEmbedding, testSegment);
                Log.infof("Store test successful. Proceeding with bulk embedding...");
                embeddedCount = 1; // Count the test embedding
            }
        } catch (jakarta.enterprise.inject.CreationException e) {
            // Catch CreationException which happens during bean creation
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException &&
                    cause.getMessage() != null &&
                    cause.getMessage().contains("indexListSize") &&
                    cause.getMessage().contains("zero")) {
                Log.errorf("PgVector dimension configuration error detected during store initialization.");
                Log.errorf("The dimension property 'quarkus.langchain4j.pgvector.dimension' is being read as 0.");
                Log.errorf("Please verify:");
                Log.errorf(
                        "1. The property is set correctly in application.properties (should be 768 for granite-embedding:278m)");
                Log.errorf("2. PostgreSQL database is running and accessible");
                Log.errorf("3. The pgvector extension is installed: CREATE EXTENSION IF NOT EXISTS vector;");
                Log.errorf("4. Try setting 'quarkus.langchain4j.pgvector.use-index=false' temporarily");
                throw new RuntimeException(
                        "PgVector store initialization failed. The dimension configuration is not being read correctly. "
                                +
                                "This usually means the dimension property is 0. Check application.properties and database configuration.",
                        e);
            }
            throw e;
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("indexListSize") && e.getMessage().contains("zero")) {
                Log.errorf("PgVector dimension configuration error. The dimension is being read as 0.");
                Log.errorf("Please verify 'quarkus.langchain4j.pgvector.dimension=768' in application.properties");
                throw new RuntimeException(
                        "PgVector dimension misconfiguration. Dimension must be > 0. Check application.properties.", e);
            }
            throw e;
        } catch (Exception e) {
            Log.errorf(e, "Failed to test embedding store. This might indicate a configuration issue.");
            throw new RuntimeException(
                    "Embedding store test failed. Please check your database and pgvector configuration.", e);
        }

        // Store the remaining segments in the embedding store by creating embeddings
        // (Skip first segment if we already tested with it)
        int startIndex = embeddedCount > 0 ? 1 : 0;
        int errorCount = 0;
        for (int i = startIndex; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            try {
                var embedding = embeddingModel.embed(segment).content();
                store.add(embedding, segment);
                embeddedCount++;
                if (embeddedCount % 10 == 0) {
                    Log.infof("Progress: embedded %d/%d segments", embeddedCount, segments.size());
                }
            } catch (Exception e) {
                errorCount++;
                Log.errorf(e, "Failed to embed and store segment: %s",
                        segment.text().substring(0, Math.min(50, segment.text().length())));
                // Continue with other segments for non-critical errors
            }
        }

        Log.infof("Successfully embedded and stored %d out of %d segments (errors: %d)", embeddedCount, segments.size(),
                errorCount);
    }

}