package com.example;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class ArticleResource {

    @Inject
    ArticleRepository repo;

    @GET
    public List<ArticleSearchResult> search(@QueryParam("q") String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return repo.search(query);
    }
}
