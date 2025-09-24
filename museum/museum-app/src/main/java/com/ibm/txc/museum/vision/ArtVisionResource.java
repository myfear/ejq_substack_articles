package com.ibm.txc.museum.vision;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import com.ibm.txc.museum.ai.ArtInspectorAi;
import com.ibm.txc.museum.utils.ImageCompressor;

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

@Path("/api/vision")
public class ArtVisionResource {

    @Inject
    ArtInspectorAi ai;

    @POST
    @Path("/describe")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response describe(@FormParam("photo") FileUpload upload) {
        try {
            
            //Compress the image to save token and bandwidth
            // or use the original image via:
            //String b64 = Base64.getEncoder().encodeToString(bytes);
            String b64 = ImageCompressor.condense(upload.uploadedFile().toFile(), 500, 0.5f);

            Image img = Image.builder()
                    .base64Data(b64)
                    .mimeType("image/jpeg")
                    .build();

            String json = ai.identify(img);

            return Response.ok(json).build();
        } catch (Exception e) {
            Log.error("Error describing image", e);
            return Response.status(400).entity("{\"error\":\"bad_image\"}").build();
        }
    }
}