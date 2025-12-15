package com.ibm.ingest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.ingest.DoclingConverter.PageContent;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
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

        // For each file in the folder, extract pages with metadata using docling
        // converter
        if (Files.exists(documentsPath) && Files.isDirectory(documentsPath)) {
            int successCount = 0;
            int failureCount = 0;
            int totalPages = 0;

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

                        // Extract pages with page-level metadata
                        List<PageContent> pages = doclingConverter.extractPages(file);

                        for (PageContent page : pages) {
                            Map<String, String> metaMap = new HashMap<>();
                            metaMap.put("file_name", fileName);
                            metaMap.put("page_number", String.valueOf(page.pageNumber()));
                            metaMap.put("format", extension);
                            // Note: sourceUrl is not available during startup loading,
                            // but can be added later via ingestWithProvenance if needed

                            Document doc = Document.document(page.text(), new Metadata(metaMap));
                            docs.add(doc);
                        }

                        totalPages += pages.size();
                        successCount++;
                        Log.infof("Successfully processed file: %s (%d pages)", file.getName(), pages.size());
                    } catch (Exception e) {
                        failureCount++;
                        Log.errorf(e, "Failed to process file: %s. Error: %s", filePath, e.getMessage());
                        // Continue processing other files instead of failing the entire startup
                    }
                }
            }

            Log.infof("Document loading completed. Success: %d, Failures: %d, Skipped: %d, Total pages: %d",
                    successCount, failureCount, skippedCount, totalPages);

            if (docs.isEmpty()) {
                Log.warn("No documents were successfully loaded. Please check the logs for errors.");
            }
        }

        // Only process if we have documents
        if (docs.isEmpty()) {
            Log.warn("No documents to process. Skipping embedding generation.");
            return;
        }

        Log.infof("Ingesting %d document pages into embedding store...", docs.size());

        // Use EmbeddingStoreIngestor for cleaner ingestion with proper splitting
        try {
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingStore(store)
                    .embeddingModel(embeddingModel)
                    .documentSplitter(DocumentSplitters.recursive(800, 100))
                    .build();

            ingestor.ingest(docs);

            Log.infof("Successfully ingested %d document pages", docs.size());
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
            Log.errorf(e,
                    "Failed to ingest documents into embedding store. This might indicate a configuration issue.");
            throw new RuntimeException(
                    "Document ingestion failed. Please check your database and pgvector configuration.", e);
        }
    }

    /**
     * Ingests a document with provenance information (source URL).
     * This method can be called programmatically to add documents with URLs at
     * runtime.
     * 
     * @param pdfFile   The physical file to parse.
     * @param sourceUrl The URL where this file can be viewed (SharePoint, S3, etc).
     */
    public void ingestWithProvenance(File pdfFile, String sourceUrl) {
        try {
            Log.infof("Extracting pages from %s", pdfFile.getName());

            List<PageContent> pages = doclingConverter.extractPages(pdfFile);
            List<Document> documents = new ArrayList<>();

            // Extract file extension
            String fileName = pdfFile.getName();
            int lastDotIndex = fileName.lastIndexOf('.');
            String extension = (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1)
                    ? fileName.substring(lastDotIndex + 1).toLowerCase()
                    : "";

            for (PageContent page : pages) {
                Map<String, String> metaMap = new HashMap<>();
                metaMap.put("file_name", fileName);
                metaMap.put("page_number", String.valueOf(page.pageNumber()));
                metaMap.put("format", extension);

                if (sourceUrl != null && !sourceUrl.isBlank()) {
                    metaMap.put("url", sourceUrl);
                }

                Document doc = Document.document(page.text(), new Metadata(metaMap));
                documents.add(doc);
            }

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingStore(store)
                    .embeddingModel(embeddingModel)
                    .documentSplitter(DocumentSplitters.recursive(800, 100))
                    .build();

            ingestor.ingest(documents);

            Log.infof("Ingested %d pages from %s", pages.size(), pdfFile.getName());

        } catch (Exception e) {
            throw new RuntimeException("Ingestion failed for " + pdfFile.getName(), e);
        }
    }

}