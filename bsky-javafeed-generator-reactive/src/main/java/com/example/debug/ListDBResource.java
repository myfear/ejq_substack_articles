package com.example.debug;

import java.util.List;

import com.example.model.PostEntity;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/debug/posts")
@Produces(MediaType.APPLICATION_JSON)
public class ListDBResource {

    @GET
    @WithTransaction
    public Uni<List<PostEntity>> getAllPosts() {
        // Query all posts from the database
        return PostEntity.findAll().list()
                .onItem().transform(posts -> posts.stream()
                        .map(entity -> (PostEntity) entity)
                        .toList())
                .onItem().invoke(posts -> {
                    Log.infof("Found %d posts in database", posts.size());
                })
                .onFailure().invoke(failure -> {
                    Log.errorf(failure, "Failed to fetch posts: %s", failure.getMessage());
                });
    }
}
