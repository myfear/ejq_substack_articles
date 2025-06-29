package org.acme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.jboss.logging.Logger;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.common.annotation.Blocking;

@jakarta.ws.rs.Path("/upload")
public class FileUploadResource {

    private static final Logger LOG = Logger.getLogger(FileUploadResource.class);

    @Inject
    Vertx vertx;
    @Inject
    UploadService uploadService;
    @Inject
    SseService sseService;

    private final Path tempDir;

    public FileUploadResource() throws IOException {
        // Create uploads directory in Maven target folder
        Path targetDir = Path.of("target");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        this.tempDir = targetDir.resolve("uploads");
        if (!Files.exists(tempDir)) {
            Files.createDirectories(tempDir);
        }
        LOG.infof("Upload temp directory: %s", tempDir.toAbsolutePath());
    }

    @POST
    @jakarta.ws.rs.Path("/chunk")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Blocking
    public Uni<Response> uploadChunk(byte[] body,
            @HeaderParam("X-Upload-Id") String uploadId,
            @HeaderParam("X-Chunk-Number") int chunkNumber,
            @HeaderParam("X-Total-Bytes") long totalBytes,
            @HeaderParam("X-Client-Id") String clientId) {

        LOG.infof("Received chunk %d for upload %s (size: %d bytes, total: %d bytes, client: %s)", 
                  chunkNumber, uploadId, body.length, totalBytes, clientId);

        if (uploadId == null || uploadId.isEmpty())
            return Uni.createFrom().item(Response.status(400).entity("Missing X-Upload-Id").build());

        if (chunkNumber == 1) {
            LOG.infof("Starting new upload %s with total size %d bytes", uploadId, totalBytes);
            uploadService.startUpload(uploadId, totalBytes);
        }

        var chunkPath = tempDir.resolve(uploadId + ".part" + chunkNumber);
        byte[] chunkData = body;

        LOG.infof("Writing chunk %d to file: %s", chunkNumber, chunkPath);

        return vertx.fileSystem()
                .writeFile(chunkPath.toString(), Buffer.buffer(chunkData))
                .onItem().transform(v -> {
                    uploadService.updateProgress(uploadId, chunkData.length);
                    var progress = uploadService.getProgress(uploadId);
                    LOG.infof("Chunk %d uploaded successfully. Progress: %d%% (%d/%d bytes)", 
                              chunkNumber, progress.getPercentage(), progress.uploadedBytes, progress.totalBytes);
                    sseService.sendProgress(clientId, progress);
                    return Response.ok("Chunk " + chunkNumber + " uploaded").build();
                });
    }

    @POST
    @jakarta.ws.rs.Path("/complete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Response> completeUpload(UploadCompletionRequest request) {
        LOG.infof("Completing upload %s: %s (%d chunks)", request.uploadId, request.fileName, request.totalChunks);
        
        Path finalPath = tempDir.resolve(request.fileName);
        try {
            // Handle existing file - create unique filename if needed
            if (Files.exists(finalPath)) {
                String baseName = request.fileName;
                String extension = "";
                int lastDot = baseName.lastIndexOf('.');
                if (lastDot > 0) {
                    extension = baseName.substring(lastDot);
                    baseName = baseName.substring(0, lastDot);
                }
                
                int counter = 1;
                do {
                    String newFileName = baseName + "_" + counter + extension;
                    finalPath = tempDir.resolve(newFileName);
                    counter++;
                } while (Files.exists(finalPath));
                
                LOG.infof("Original file exists, using unique name: %s", finalPath.getFileName());
            }
            
            Files.createFile(finalPath);
            LOG.infof("Created final file: %s", finalPath);
            
            for (int i = 1; i <= request.totalChunks; i++) {
                Path chunk = tempDir.resolve(request.uploadId + ".part" + i);
                LOG.debugf("Processing chunk %d: %s", i, chunk);
                Files.write(finalPath, Files.readAllBytes(chunk), StandardOpenOption.APPEND);
                Files.delete(chunk);
                LOG.debugf("Merged and deleted chunk %d", i);
            }
            uploadService.finishUpload(request.uploadId);
            LOG.infof("Upload %s completed successfully. Final file: %s", request.uploadId, finalPath);
            return Uni.createFrom().item(Response.ok("Upload complete").build());
        } catch (IOException e) {
            LOG.errorf(e, "Failed to complete upload %s", request.uploadId);
            return Uni.createFrom().item(Response.serverError().entity(e.getMessage()).build());
        }
    }

    public record UploadCompletionRequest(String uploadId, String fileName, int totalChunks) {}
}