package com.ibm.ingest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
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

    @ConfigProperty(name = "document.endpoint")
    String documentEndpoint;

    @PostConstruct
    void loadDocument() throws Exception {

        Log.infof("Starting document loading...");

        // Read the files from /resources/documents folder
        Path documentsPath = Path.of("src/main/resources/documents");

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

                        // Extract TextSegments with page-level metadata
                        List<TextSegment> segments = doclingConverter.extractPages(file);

                        // Add additional metadata (file_name, format) to each segment
                        for (TextSegment segment : segments) {
                            // Build enhanced metadata map
                            Map<String, String> metaMap = new HashMap<>();

                            // Copy existing metadata (we know the keys from DoclingConverter)
                            Metadata existingMetadata = segment.metadata();
                            if (existingMetadata != null) {
                                // Extract known metadata keys
                                String docId = existingMetadata.getString("doc_id");
                                String source = existingMetadata.getString("source");
                                String pageNumber = existingMetadata.getString("page_number");

                                if (docId != null)
                                    metaMap.put("doc_id", docId);
                                if (source != null)
                                    metaMap.put("source", source);
                                if (pageNumber != null)
                                    metaMap.put("page_number", pageNumber);
                            }

                            // Add additional metadata
                            metaMap.put("file_name", fileName);
                            metaMap.put("format", extension);
                            metaMap.put("source_url", documentEndpoint + "/" + fileName);

                            // Create new segment with enhanced metadata
                            TextSegment enhancedSegment = TextSegment.from(segment.text(), Metadata.from(metaMap));

                            // Generate embedding
                            Embedding embedding = embeddingModel.embed(enhancedSegment).content();

                            // Store with metadata
                            store.add(embedding, enhancedSegment);
                        }

                        totalPages += segments.size();
                        successCount++;
                        Log.infof("Successfully processed file: %s (%d pages)", file.getName(), segments.size());
                    } catch (Exception e) {
                        failureCount++;
                        Log.errorf(e, "Failed to process file: %s. Error: %s", filePath, e.getMessage());
                        // Continue processing other files instead of failing the entire startup
                    }
                }
            }

            Log.infof("Document loading completed. Success: %d, Failures: %d, Skipped: %d, Total pages: %d",
                    successCount, failureCount, skippedCount, totalPages);
        }
    }

}