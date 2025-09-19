package com.ibm.txc.museum.sentiment;

import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.util.Base64;

import org.jboss.resteasy.reactive.multipart.FileUpload;

import com.ibm.txc.museum.ai.SentimentAi;
import com.ibm.txc.museum.domain.Art;
import com.ibm.txc.museum.domain.SentimentVote;

import dev.langchain4j.data.image.Image;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/sentiment")
public class SentimentResource {

    @Inject
    SentimentAi ai;

    @POST
    @Path("/{code}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response vote(@PathParam("code") String code,
            @FormParam("selfie") FileUpload upload,
            @FormParam("impression") String impression,
            @HeaderParam("X-Forwarded-For") String xff,
            @HeaderParam("X-Real-IP") String xreal,
            @HeaderParam("User-Agent") String ua,
            @HeaderParam("Host") String host,
            @HeaderParam("Referer") String ref) {
        Art art = Art.find("code", code).firstResult();
        if (art == null)
            return Response.status(404).build();

        String clientIp = pickIp(xff, xreal);

        try {
            byte[] bytes = Files.readAllBytes(upload.uploadedFile());
            String b64 = Base64.getEncoder().encodeToString(bytes);
            Image img = Image.builder()
                    .base64Data(b64)
                    .mimeType("image/jpeg")
                    .build();

            String label = ai.classify(img, impression == null ? "" : impression);

            SentimentVote v = new SentimentVote();
            v.art = art;
            v.label = label.trim();
            v.createdAt = OffsetDateTime.now();
            v.ip = clientIp == null ? "unknown" : clientIp;
            v.persist();

            return Response.ok("{\"ok\":true,\"label\":\"" + v.label + "\"}").build();
        } catch (Exception e) {
            return Response.status(400).entity("{\"error\":\"bad_image_or_model\"}").build();
        }
    }

    private String pickIp(String xff, String xreal) {
        if (xreal != null && !xreal.isBlank())
            return xreal;
        if (xff != null && !xff.isBlank())
            return xff.split(",")[0].trim();
        return null;
    }
}