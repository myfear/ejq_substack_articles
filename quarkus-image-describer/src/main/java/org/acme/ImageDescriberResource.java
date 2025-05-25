package org.acme;

import java.io.IOException;
import java.nio.file.Files;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import dev.langchain4j.data.image.Image;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/describe-image")
public class ImageDescriberResource {

    public static Logger LOG = Logger.getLogger(ImageDescriberResource.class);

    @Inject
    ImageDescriberAiService imageService;

    @POST
    @Path("/describe")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String describeImage(@RestForm("image") FileUpload file) {
        if (file == null) {
            return "Error: No file uploaded.";
        }

        try {
            // 1. Read image bytes from the uploaded file
            byte[] imageBytes = Files.readAllBytes(file.uploadedFile());

            // 2. Get the MIME type of the uploaded image
            String mimeType = file.contentType();
            if (mimeType == null || (!mimeType.equals("image/png") && !mimeType.equals("image/jpeg"))) {
                // Add more supported types if needed by your model
                return "Error: Only PNG and JPEG images are supported. Uploaded type: " + mimeType;
            }

            LOG.info(mimeType + " image received for description.");
            String base64String = java.util.Base64.getEncoder().encodeToString(imageBytes);
            LOG.info(base64String + " b64");

            // 3. Create a Langchain4j Image object
            Image langchainImage = Image.builder()
                    .base64Data(base64String) // Use the base64 encoded string
                    .mimeType(mimeType)
                    .build();

            // 4. Call the AI service
            String imageDescription = imageService.describeImage(langchainImage);
            return imageDescription;

        } catch (IOException e) {
            // Log the exception (e.g., using org.jboss.logging.Logger)
            e.printStackTrace();
            return "Error processing image: " + e.getMessage();
        } catch (Exception e) {
            // Catch other potential exceptions from the AI service
            e.printStackTrace();
            return "Error getting description from AI: " + e.getMessage();
        }
    }

}
