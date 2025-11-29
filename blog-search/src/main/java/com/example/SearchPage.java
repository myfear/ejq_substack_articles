package com.example;

import java.util.List;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/ui/search")
public class SearchPage {

    @Inject
    ArticleRepository repo;

    @Inject
    Template search; // matches search.html

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance page(@QueryParam("q") String q) {

        List<ArticleSearchResult> results = (q == null || q.isBlank()) ? null : repo.search(q);

        return search
                .data("q", q)
                .data("results", results);
    }
}