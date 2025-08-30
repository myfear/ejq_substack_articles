package org.acme.critic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import javax.imageio.ImageIO;

import org.acme.critic.ai.CompositionCritic;
import org.acme.critic.golden.ImageOverlay;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import dev.langchain4j.data.image.Image;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/critic")
public class ImageAnalysisResource {

    public static final double PHI = 1.61803398875;

    @Inject
    CompositionCritic critic;

    @POST
    @Path("/extract")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadAndAnalyze(@RestForm("image") FileUpload file) throws IOException {

        try {

            // 0. Check if we received a file
            if (file == null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"error\":\"Error: No file uploaded.\"}").build();
            }

            // 2. Get the MIME type of the uploaded image
            String mimeType = file.contentType();
            if (mimeType == null || (!mimeType.equals("image/png") && !mimeType.equals("image/jpeg"))) {
                // Add more supported types if needed by your model
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Only PNG and JPEG images are supported. Uploaded type: " + mimeType
                                + "\"}")
                        .build();
            }

            Log.info(mimeType + " image received for description.");

            // 3. Read image bytes from the uploaded file
            try (InputStream inputStream = Files.newInputStream(file.uploadedFile())) {
                byte[] imageBytes = inputStream.readAllBytes();
                Image image = Image.builder()
                        .base64Data(java.util.Base64.getEncoder().encodeToString(imageBytes))
                        .build();
                return Response.ok(critic.critiqueComposition(image)).build();
            }
        } catch (Exception e) {
            Log.error("Error processing the image", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Could not process the image.\"}").build();
        }

    }

    @POST
    @Path("/overlay")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response createGoldenGridOverlay(@RestForm("image") FileUpload file) throws IOException {

        try (InputStream inputStream = Files.newInputStream(file.uploadedFile())) {
            BufferedImage img = ImageIO.read(inputStream);
            ImageOverlay.draw(img);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return Response.ok(baos.toByteArray())
                    .type(MediaType.APPLICATION_OCTET_STREAM)
                    .build();
        }

    }

    public static class FileUploadInput {

        @FormParam("file")
        public List<FileUpload> file;

    }

}
