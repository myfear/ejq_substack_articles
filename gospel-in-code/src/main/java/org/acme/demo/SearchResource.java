package org.acme.demo;

import java.util.List;

import org.acme.Verse;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/search")
public class SearchResource {

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    Template search;

    @Inject
    EntityManager em;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String search(@QueryParam("q") String query) {
        if (query == null || query.isBlank()) {
            return search.data("query", null).data("results", List.of()).render();
        }
        float[] queryEmbedding = embeddingModel.embed(query).content().vector();

    
        Log.info(queryEmbedding.length + " dimensions for query: " + query);

        List<Verse> verses = em
                .createQuery("FROM Verse ORDER BY l2_distance(embedding, :embedding) LIMIT 5", Verse.class)
                .setParameter("embedding", queryEmbedding)
                .getResultList();

        Log.info(verses.size() + " results found for query: " + query);
        return search.data("query", query)
                .data("results", verses).render();
    }
}