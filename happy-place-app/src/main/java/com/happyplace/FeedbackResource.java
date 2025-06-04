package com.happyplace;

import com.happyplace.services.SentimentService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/feedback")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackResource {

    @Inject
    SentimentService sentimentService;

    // Simple DTO for receiving post text
    public static class FeedbackPayload {
        public String text;
    }

    @POST
    @Path("/like")
    public Uni<Response> likePost(FeedbackPayload payload) {
        if (payload == null || payload.text == null || payload.text.isBlank()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Post text is required\"}").build());
        }
        return sentimentService.recordLike(payload.text)
            .invoke(() -> System.out.println("Like processing initiated for: "
                    + payload.text.substring(0, Math.min(payload.text.length(), 50)) + "..."))
            .replaceWith(Response.ok("{\"message\":\"Like processing initiated\"}").build());
    }

    @POST
    @Path("/dislike")
    public Uni<Response> dislikePost(FeedbackPayload payload) {
        if (payload == null || payload.text == null || payload.text.isBlank()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Post text is required\"}").build());
        }
        return sentimentService.recordDislike(payload.text)
            .invoke(() -> System.out.println("Dislike processing initiated for: "
                    + payload.text.substring(0, Math.min(payload.text.length(), 50)) + "..."))
            .replaceWith(Response.ok("{\"message\":\"Dislike processing initiated\"}").build());
    }
}