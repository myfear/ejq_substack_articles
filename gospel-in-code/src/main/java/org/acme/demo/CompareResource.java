package org.acme.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.acme.Verse;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.CosineSimilarity;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class CompareResource {

    @Inject
    Template index; // Injects the index.html template

    // A record to hold our data structure for the template
    public record VerseComparison(List<Verse> verses, Map<String, String> similarity) {
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get(
            @QueryParam("book") String book,
            @QueryParam("chapter") Optional<Integer> chapter,
            @QueryParam("verse") Optional<Integer> verse) {

        // Check if all parameters for a comparison are present
        if (book != null && !book.isBlank() && chapter.isPresent() && verse.isPresent()) {

            // --- This is the logic from the old CompareResource ---
            List<Verse> verses = Verse.find(
                    "book = ?1 and chapter = ?2 and verseNum = ?3 and embedding is not null",
                    book, chapter.get(), verse.get()).list();

            // iterate through the verses and log them
            Log.info("Found " + verses.size() + " verses for comparison:");
            for (Verse v : verses) {
                Log.info("Verse: " + v.translation + " " + v.book + " " + v.getEmbedding());
            }

            Map<String, String> formattedSimilarityScores = new HashMap<>();
            for (int i = 0; i < verses.size(); i++) {
                for (int j = i + 1; j < verses.size(); j++) {
                    Verse a = verses.get(i);
                    Verse b = verses.get(j);

                    double score = CosineSimilarity.between(Embedding.from(a.getEmbedding()),
                            Embedding.from(b.getEmbedding()));
                    String key = a.translation + "↔" + b.translation;
                    formattedSimilarityScores.put(key, String.format("%.4f", score));
                }
            }
            VerseComparison comparisonResult = new VerseComparison(verses, formattedSimilarityScores);
            // --- End of comparison logic ---

            // Render the index template and pass the result data to it
            return index.data("comparisonResult", comparisonResult).render();
        }

        // If no parameters, just render the page without any comparison results
        return index.data("comparisonResult", null).render();
    }

    @GET
    @Path("/api/verses")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Verse> getVersesJson(
            @QueryParam("book") String book,
            @QueryParam("chapter") Optional<Integer> chapter,
            @QueryParam("verse") Optional<Integer> verse) {
        if (book != null && !book.isBlank() && chapter.isPresent() && verse.isPresent()) {
            return Verse.find(
                    "book = ?1 and chapter = ?2 and verseNum = ?3",
                    book, chapter.get(), verse.get()).list();
        }
        return List.of();
    }
}