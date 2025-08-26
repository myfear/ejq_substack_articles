package com.example.api;

import java.util.List;

import com.example.model.PostEntity;

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
    public FeedResponse getFeedSkeleton(@QueryParam("feed") String feedUri,
            @QueryParam("cursor") String cursor,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        // Query recent posts from the database (sorted by newest first)
        List<PostEntity> posts;
        if (cursor != null && !cursor.isEmpty()) {
            // TODO: optional pagination logic (not fully implemented in this prototype)
            posts = PostEntity.findAll().list(); // (for now, ignore cursor in prototype)
        } else {
            posts = PostEntity.findAll().page(0, limit).list();
            // Alternatively: PostEntity.findAll(Sort.by("createdAt").descending()).range(0,
            // limit-1).list();
        }

        // Build the feed list
        FeedResponse response = new FeedResponse();
        response.feed = posts.stream().map(
                p -> {
                    FeedItem item = new FeedItem();
                    item.post = p.uri;
                    return item;
                }).toList();

        // For simplicity, we won’t implement real cursor pagination here.
        response.cursor = "";
        return response;
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
