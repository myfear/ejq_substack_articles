package org.acme;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import io.quarkiverse.antivirus.runtime.AntivirusScanResult;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@jakarta.ws.rs.Path("/files")
public class FileUploadResource {

    @Inject
    VirusScannerService scannerService;

    @POST
    @jakarta.ws.rs.Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> uploadFile(@RestForm("file") FileUpload file) {

        Log.infof("Received file upload request for: %s (size: %d bytes)",
                file.fileName(), file.size());

        return Uni.createFrom().<InputStream>item(() -> {
            try {
                // Read the entire file into memory for virus scanning
                // ByteArrayInputStream fully supports mark/reset operations required by ClamAV
                // This ensures we can scan the file content before any filesystem storage
                InputStream fileStream = java.nio.file.Files.newInputStream(file.uploadedFile());
                byte[] fileBytes = fileStream.readAllBytes();
                fileStream.close(); // Close the file stream immediately
                return new ByteArrayInputStream(fileBytes);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read uploaded file: " + e.getMessage(), e);
            }
        })
                .runSubscriptionOn(io.smallrye.mutiny.infrastructure.Infrastructure.getDefaultWorkerPool())
                .onItem().transformToUni(inputStream -> {
                    // Perform virus scanning reactively using the input stream
                    return scannerService.scanFileReactive(file.fileName(), inputStream)
                            .onItem().invoke(() -> {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    Log.warn("Warning: Failed to close input stream: " + e.getMessage());
                                }
                            });
                })
                .onItem().transformToUni(scanResults -> {
                    // Process scan results reactively
                    return Uni.createFrom().item(() -> {
                        // Check if any scanner found a threat
                        for (AntivirusScanResult result : scanResults) {
                            if (result.getStatus() != Response.Status.OK.getStatusCode()) {
                                Log.warnf("THREAT DETECTED in %s: %s", file.fileName(), result.getMessage());

                                return Response.status(result.getStatus())
                                        .entity("{\"status\": \"THREAT_DETECTED\", \"message\": \"" +
                                                result.getMessage() + "\", \"filename\": \"" +
                                                file.fileName() + "\"}")
                                        .build();
                            }
                        }

                        // File is clean - now we can safely process it
                        Log.infof("File is clean: %s", file.fileName());

                        // Here you would typically:
                        // 1. Move the file to permanent storage
                        // 2. Save metadata to database
                        // 3. Process the file further

                        return Response.ok()
                                .entity("{\"status\": \"CLEAN\", \"filename\": \"" +
                                        file.fileName() + "\", \"size\": " +
                                        file.size() + ", \"contentType\": \"" +
                                        file.contentType() + "\"}")
                                .build();
                    });
                })
                .onFailure().recoverWithItem(throwable -> {
                    Log.errorf("Error during file processing for %s: %s",
                            file.fileName(), throwable.getMessage());

                    return Response.serverError()
                            .entity("{\"status\": \"ERROR\", \"message\": \"File processing failed: " +
                                    throwable.getMessage() + "\"}")
                            .build();
                });
    }
}