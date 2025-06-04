package com.happyplace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.happyplace.services.AIService;
import com.happyplace.services.SentimentService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.time.Duration;
import java.util.UUID;
import java.util.List; // For initial posts list
import java.util.stream.Collectors;

@Path("/api/posts") // Updated path from /api/thoughts
public class HappyResource {

    @Inject
    AIService aiService;

    @Inject
    SentimentService sentimentService;

    @Inject
    ObjectMapper objectMapper; // For JSON conversion

    // Simple Post record for structure
    public record Post(String id, String text) {
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<String> streamHappyPosts(@QueryParam("count") int count) {
        int requestedCount = (count <= 0 || count > 10) ? 3 : count;

        return Multi.createFrom().ticks().every(Duration.ofMillis(200))
                .onItem().transformToUniAndMerge(tick ->
                    Uni.createFrom().item(() -> {
                        String preferences = sentimentService.getAggregatedPreferences();
                        String thought;
                        if (preferences.isBlank()) {
                            thought = aiService.generateHappyThought()
                                    .collect().asList().await().indefinitely()
                                    .stream().collect(Collectors.joining());
                        } else {
                            thought = aiService.generateHappyThoughtWithPreferences(preferences)
                                    .collect().asList().await().indefinitely()
                                    .stream().collect(Collectors.joining());
                        }
                        return new Post(UUID.randomUUID().toString(), thought);
                    }).runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                )
                .map(post -> {
                    try {
                        return objectMapper.writeValueAsString(post);
                    } catch (JsonProcessingException e) {
                        System.err.println("Error serializing post: " + e.getMessage());
                        return "{\"error\":\"Failed to serialize post\", \"id\":\"error-" + UUID.randomUUID().toString() + "\"}";
                    }
                })
                .select().first(requestedCount);
    }

    @GET
    @Path("/initial")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<Post>> getInitialPosts(@QueryParam("count") int count) {
        int requestedCount = (count <= 0 || count > 5) ? 3 : count;

        return Multi.createBy().repeating()
                .uni(() -> Uni.createFrom().item(() -> {
                    String preferences = sentimentService.getAggregatedPreferences();
                    String thought;
                    if (preferences.isBlank()) {
                        thought = aiService.generateHappyThought()
                                .collect().asList().await().indefinitely()
                                .stream().collect(Collectors.joining());
                    } else {
                        thought = aiService.generateHappyThoughtWithPreferences(preferences)
                                .collect().asList().await().indefinitely()
                                .stream().collect(Collectors.joining());
                    }
                    return new Post(UUID.randomUUID().toString(), thought);
                }))
                .atMost(requestedCount)
                .collect().asList();
    }
}