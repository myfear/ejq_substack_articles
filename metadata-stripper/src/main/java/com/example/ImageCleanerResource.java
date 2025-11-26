package com.example;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/clean")
public class ImageCleanerResource {

    @Inject
    Logger log;

    @Inject
    MetadataReader metadataReader;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/jpeg")
    public Response cleanImage(@FormParam("file") FileUpload file) {
        if (file == null) {
            throw new WebApplicationException("Missing file upload", 400);
        }

        String filename = file.fileName();
        byte[] originalBytes;
        try {
            originalBytes = Files.readAllBytes(file.uploadedFile());
        } catch (IOException e) {
            log.errorf(e, "Failed to read uploaded file '%s': %s", filename, e.getMessage());
            throw new WebApplicationException("Failed to read uploaded file: " + e.getMessage(),
                    Response.Status.BAD_REQUEST);
        }

        // Log metadata before stripping
        logMetadata("BEFORE", originalBytes, filename);

        // Validate that the file is actually an image by trying to read it
        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(originalBytes));
            if (image == null) {
                throw new IOException("File is not a valid image format");
            }
            log.debugf("Read image: %dx%d", image.getWidth(), image.getHeight());
        } catch (IOException e) {
            log.errorf(e, "Failed to read image from file '%s': %s", filename, e.getMessage());
            throw new WebApplicationException("Failed to read image: " + e.getMessage(),
                    Response.Status.BAD_REQUEST);
        }

        // Check if image has metadata - if not, return original without processing
        if (!hasMetadata(originalBytes)) {
            log.debugf("No metadata found in file '%s', returning original image", filename);
            logMetadata("AFTER", originalBytes, filename);
            return Response.ok(originalBytes, MediaType.valueOf("image/jpeg"))
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .build();
        }

        // Write image as JPEG (no metadata)
        byte[] outputBytes;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "jpg", baos)) {
                throw new IOException("No JPEG writer available");
            }
            outputBytes = baos.toByteArray();
            log.debugf("Wrote JPEG image: %d bytes", outputBytes.length);
        } catch (IOException e) {
            log.errorf(e, "Failed to write JPEG image for file '%s': %s", filename, e.getMessage());
            throw new WebApplicationException("Failed to write image: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

        // Log metadata after stripping
        logMetadata("AFTER", outputBytes, filename);

        return Response.ok(outputBytes, MediaType.valueOf("image/jpeg"))
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .build();
    }

    private void logMetadata(String stage, byte[] imageBytes, String filename) {
        try {
            Map<String, Object> metadata = metadataReader.readMetadata(imageBytes);
            log.infof("Metadata %s stripping for file '%s': %s", stage, filename, metadata);
        } catch (IOException e) {
            log.warnf("Failed to read metadata %s stripping for file '%s': %s", stage, filename, e.getMessage());
        } catch (Exception e) {
            // Catch any other exceptions (like IllegalArgumentException) that might be
            // thrown
            // by the underlying imaging library when it can't parse the format
            log.debugf("Could not parse metadata %s stripping for file '%s' (this is expected for cleaned images): %s",
                    stage, filename, e.getMessage());
        }
    }

    private boolean hasMetadata(byte[] imageBytes) {
        try {
            Map<String, Object> metadata = metadataReader.readMetadata(imageBytes);
            Boolean hasMetadata = (Boolean) metadata.get("hasMetadata");
            return hasMetadata != null && hasMetadata;
        } catch (IOException e) {
            log.debugf(e, "Failed to check metadata, assuming metadata exists: %s", e.getMessage());
            return true;
        } catch (Exception e) {
            // If we can't parse the image at all (e.g., IllegalArgumentException from
            // imaging library),
            // assume it has no metadata
            log.debugf("Could not parse image format, assuming no metadata: %s", e.getMessage());
            return false;
        }
    }
}