package com.ibm.ingest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.docling.serve.api.chunk.response.Chunk;
import ai.docling.serve.api.chunk.response.ChunkDocumentResponse;
import ai.docling.serve.api.convert.request.options.OutputFormat;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import io.quarkiverse.docling.runtime.client.DoclingService;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;

@ApplicationScoped
public class DoclingConverter {

    @Inject
    DoclingService doclingService;

    public String toMarkdown(File sourceFile) throws IOException {
        Path filePath = sourceFile.toPath();

        try {
            var response = doclingService.convertFile(filePath, OutputFormat.MARKDOWN);
            var document = response.getDocument();
            return document.getMarkdownContent();
        } catch (ProcessingException e) {
            // Check and log health status when there's a connection error
            try {
                boolean isHealthy = doclingService.isHealthy();
                Log.warnf("Docling service health check: %s (file: %s)",
                        isHealthy ? "HEALTHY" : "UNHEALTHY", sourceFile.getName());
            } catch (Exception healthCheckException) {
                Log.warnf("Failed to check Docling service health status: %s (file: %s)",
                        healthCheckException.getMessage(), sourceFile.getName());
            }

            Throwable cause = e.getCause();
            String errorMessage = cause != null ? cause.getMessage() : e.getMessage();
            throw new IOException("Failed to convert file: " + sourceFile + ". Cause: " + errorMessage, e);
        } catch (Exception e) {
            // Check and log health status for any exception
            try {
                boolean isHealthy = doclingService.isHealthy();
                Log.warnf("Docling service health check: %s (file: %s)",
                        isHealthy ? "HEALTHY" : "UNHEALTHY", sourceFile.getName());
            } catch (Exception healthCheckException) {
                Log.warnf("Failed to check Docling service health status: %s (file: %s)",
                        healthCheckException.getMessage(), sourceFile.getName());
            }

            throw new IOException("Failed to convert file: " + sourceFile, e);
        }
    }

    /**
     * Extracts content page by page and returns TextSegments with Docling metadata.
     */
    public List<TextSegment> extractPages(File sourceFile) throws IOException {
        Path filePath = sourceFile.toPath();
        List<TextSegment> resultSegments = new ArrayList<>();
        String fileName = sourceFile.getName();

        try {
            // Use hybridChunkFromUri to get ChunkDocumentResponse with chunks
            ChunkDocumentResponse chunkResponse = doclingService.chunkFileHybrid(filePath, OutputFormat.MARKDOWN);

            List<Chunk> chunks = chunkResponse.getChunks();

            // Map to group chunks by page number
            Map<Integer, StringBuilder> pageTextMap = new HashMap<>();

            if (chunks != null) {
                for (Chunk chunk : chunks) {
                    // Get text content from chunk
                    String chunkText = chunk.getText();

                    // Get page numbers from chunk (returns List<Integer>)
                    List<Integer> pageNumbers = chunk.getPageNumbers();

                    if (chunkText != null && !chunkText.isBlank() && pageNumbers != null && !pageNumbers.isEmpty()) {
                        // A chunk can span multiple pages, so we add it to each page it belongs to
                        for (Integer pageNumber : pageNumbers) {
                            pageTextMap.computeIfAbsent(pageNumber, k -> new StringBuilder())
                                    .append(chunkText).append("\n\n");
                        }
                    }
                }
            }

            // Convert map to list of TextSegments with metadata, sorted by page number
            pageTextMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        String combinedText = entry.getValue().toString().trim();
                        if (!combinedText.isBlank()) {
                            int pageNumber = entry.getKey();

                            // Create TextSegment with Docling metadata
                            Map<String, String> metaMap = new HashMap<>();
                            metaMap.put("doc_id", fileName);
                            metaMap.put("source", fileName);
                            metaMap.put("page_number", String.valueOf(pageNumber));

                            Metadata metadata = Metadata.from(metaMap);
                            TextSegment segment = TextSegment.from(combinedText, metadata);
                            resultSegments.add(segment);
                        }
                    });

            return resultSegments;
        } catch (ProcessingException e) {
            handleError(sourceFile, e);
            throw new IOException("Failed to convert file: " + sourceFile, e);
        } catch (Exception e) {
            handleError(sourceFile, e);
            throw new IOException("Failed to convert file: " + sourceFile, e);
        }
    }

    private void handleError(File file, Exception e) {
        Log.warnf("Docling conversion failed for file %s: %s",
                file.getName(), e.getMessage());
        try {
            boolean isHealthy = doclingService.isHealthy();
            Log.warnf("Docling service health check: %s (file: %s)",
                    isHealthy ? "HEALTHY" : "UNHEALTHY", file.getName());
        } catch (Exception healthCheckException) {
            Log.warnf("Failed to check Docling health: %s",
                    healthCheckException.getMessage());
        }
    }
}
