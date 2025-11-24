package com.example.resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import com.example.service.PolaroidService;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/polaroid")
public class PolaroidResource {

    @Inject
    PolaroidService polaroidService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    public Response createPolaroid(@RestForm("image") FileUpload image) {
        if (image == null || image.fileName() == null || image.contentType() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No image file provided. Please upload an image using the 'image' form field.")
                    .build();
        }

        String fileName = image.fileName();
        String contentType = image.contentType();

        if (!contentType.matches("image/(png|jpeg|jpg)")) {
            Log.debug("Unsupported file type: " + fileName + " (type: " + contentType + ")");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Unsupported file type. Please upload a PNG or JPG image.")
                    .build();
        }

        try {
            Log.debug("Processing uploaded image: " + fileName + " (type: " + contentType + ")");

            // Read the uploaded file into a byte array
            byte[] imageData;
            try (InputStream inputStream = Files.newInputStream(image.uploadedFile())) {
                imageData = inputStream.readAllBytes();
            }

            // Create polaroid effect
            // With custom parameters
            byte[] polaroid = polaroidService.createPolaroidFromBytes(
                    imageData,
                    "rgb(248, 248, 248)", // border color
                    "rgba(0, 0, 0, 0.4)", // background color
                    "Arial", // font name
                    30.0, // font size
                    "My Caption", // caption
                    5.0, // rotation angle
                    0L, // max thumb size (use percentage)
                    15, // 15% thumbnail
                    true, // resize after
                    true // transparent background
            );

            Log.info("Successfully created polaroid image: " + polaroid.length + " bytes");

            // Return the PNG image
            return Response.ok(polaroid)
                    .type("image/png")
                    .header("Content-Disposition", "attachment; filename=\"polaroid_" + fileName + "\"")
                    .build();

        } catch (IOException e) {
            Log.error("Failed to read uploaded file", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to read uploaded file: " + e.getMessage())
                    .build();
        } catch (RuntimeException e) {
            Log.error("Failed to create polaroid", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create polaroid: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/collage")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    public Response createCollage(@RestForm("images") List<FileUpload> images) {
        if (images == null || images.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No images provided. Please upload at least one image.")
                    .build();
        }

        // Validate all files
        for (FileUpload image : images) {
            if (image == null || image.fileName() == null || image.contentType() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid file in upload.")
                        .build();
            }

            String contentType = image.contentType();
            if (!contentType.matches("image/(png|jpeg|jpg)")) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Unsupported file type: " + image.fileName() +
                                ". Please upload PNG or JPG images only.")
                        .build();
            }
        }

        try {
            Log.info("Creating collage from " + images.size() + " images");

            // Read all uploaded images into byte arrays
            List<byte[]> imageDataList = new ArrayList<>();
            for (FileUpload image : images) {
                try (InputStream inputStream = Files.newInputStream(image.uploadedFile())) {
                    byte[] imageData = inputStream.readAllBytes();
                    imageDataList.add(imageData);
                }
            }

            // Create collage using the service
            byte[] collage = polaroidService.createCollageFromBytes(imageDataList);

            Log.info("Successfully created collage: " + collage.length + " bytes");

            return Response.ok(collage)
                    .type("image/png")
                    .header("Content-Disposition", "attachment; filename=\"polaroid_collage.png\"")
                    .build();

        } catch (IOException e) {
            Log.error("Failed to read uploaded files", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to read uploaded files: " + e.getMessage())
                    .build();
        } catch (RuntimeException e) {
            Log.error("Failed to create collage", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create collage: " + e.getMessage())
                    .build();
        }
    }
}
