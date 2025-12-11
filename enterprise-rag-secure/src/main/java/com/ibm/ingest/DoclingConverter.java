package com.ibm.ingest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import ai.docling.serve.api.convert.request.options.OutputFormat;
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
}