package com.example.api;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Base64;

import com.example.model.PostEntity;

import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/xrpc/app.bsky.feed.getFeedSkeleton")
@Produces(MediaType.APPLICATION_JSON)
public class FeedResource {

    @GET
    public Uni<FeedResponse> getFeedSkeleton(@QueryParam("feed") String feedUri,
            @QueryParam("cursor") String cursor,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        
        // Validate and clamp limit according to ATProto spec (max 100)
        int clampedLimit = Math.min(Math.max(1, limit), 100);
        
        // Build query with proper sorting (newest first)
        PanacheQuery<PostEntity> query = PostEntity.findAll(Sort.by("createdAt").descending().and("id").descending());
        
        // Apply cursor-based pagination
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                CursorInfo cursorInfo = decodeCursor(cursor);
                // Use proper Panache filter syntax with named parameters
                query = query.filter("createdAt < :timestamp OR (createdAt = :timestamp AND id < :postId)", 
                                   io.quarkus.panache.common.Parameters.with("timestamp", cursorInfo.timestamp)
                                                                      .and("postId", cursorInfo.postId));
            } catch (Exception e) {
                // If cursor is invalid, return first page
                // In production, you might want to return an error response
            }
        }
        
        // Execute query with limit
        Uni<List<PostEntity>> posts = query.range(0, clampedLimit - 1).list();
        
        // Build the feed list reactively
        return posts.map(postList -> {
            FeedResponse response = new FeedResponse();
            response.feed = postList.stream().map(
                    p -> {
                        FeedItem item = new FeedItem();
                        item.post = p.uri;
                        return item;
                    }).toList();

            // Generate next cursor if there are more posts
            response.cursor = generateNextCursor(postList, clampedLimit);
            return response;
        });
    }
    
    /**
     * Generates the next cursor for pagination.
     * Returns empty string if no more posts available.
     */
    private String generateNextCursor(List<PostEntity> posts, int limit) {
        if (posts.size() < limit) {
            return ""; // No more posts to fetch
        }
        
        PostEntity lastPost = posts.get(posts.size() - 1);
        return encodeCursor(lastPost.createdAt, lastPost.id);
    }
    
    /**
     * Encodes timestamp and post ID into a cursor string.
     * Format: Base64 encoded "timestamp:postId"
     */
    private String encodeCursor(OffsetDateTime timestamp, Long postId) {
        String cursorData = timestamp.toString() + ":" + postId;
        return Base64.getEncoder().encodeToString(cursorData.getBytes());
    }
    
    /**
     * Decodes cursor string back to timestamp and post ID.
     */
    private CursorInfo decodeCursor(String cursor) {
        String decoded = new String(Base64.getDecoder().decode(cursor));
        String[] parts = decoded.split(":", 2);
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cursor format");
        }
        
        OffsetDateTime timestamp = OffsetDateTime.parse(parts[0]);
        Long postId = Long.parseLong(parts[1]);
        
        return new CursorInfo(timestamp, postId);
    }
    
    /**
     * Internal class to hold cursor information.
     */
    private static class CursorInfo {
        final OffsetDateTime timestamp;
        final Long postId;
        
        CursorInfo(OffsetDateTime timestamp, Long postId) {
            this.timestamp = timestamp;
            this.postId = postId;
        }
    }

    // DTO classes for JSON serialization
    public static class FeedResponse {
        public List<FeedItem> feed;
        public String cursor;
    }

    public static class FeedItem {
        public String post;
        // (optionally could include "reason" or other fields if needed)
    }
}
