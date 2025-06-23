// src/main/java/org/acme/SentimentResource.java
package org.acme.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.acme.SentimentAnalyzer;
import org.acme.Verse;

@Path("/sentiment")
public class SentimentResource {

    @Inject
    Template sentiment; // Injects sentiment.html

    @Inject
    SentimentAnalyzer analyzer;

    @Inject
    ObjectMapper objectMapper;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get(
            @QueryParam("book") String book,
            @QueryParam("start_chapter") Optional<Integer> startChapter,
            @QueryParam("start_verse") Optional<Integer> startVerse,
            @QueryParam("end_chapter") Optional<Integer> endChapter,
            @QueryParam("end_verse") Optional<Integer> endVerse) {

        Log.info(endVerse + " " + startChapter + " " + startVerse + " " + endChapter + " " + book);


        if (book != null && !book.isBlank() && startChapter.isPresent() && startVerse.isPresent() && endChapter.isPresent() && endVerse.isPresent()) {
            Map<String, Map<String, Integer>> result = new TreeMap<>();
            List<Verse> verses = Verse.find(
                    "book = ?1 and ((chapter = ?2 and verseNum >= ?3) or (chapter > ?2 and chapter < ?4) or (chapter = ?4 and verseNum <= ?5))",
                    book, startChapter.get(), startVerse.get(), endChapter.get(), endVerse.get()).list();

            for (Verse v : verses) {
                // Pass the verse text directly to the analyzer (parameter must be named 'text' in the interface)
                String sentimentValue = analyzer.analyzeSentiment(v.text).toUpperCase();
                if (sentimentValue.contains("POSITIVE")) sentimentValue = "POSITIVE";
                else if (sentimentValue.contains("NEGATIVE")) sentimentValue = "NEGATIVE";
                else sentimentValue = "NEUTRAL";
                result.computeIfAbsent(v.translation, k -> new TreeMap<>()).merge(sentimentValue, 1, Integer::sum);
            }

            try {
                String chartDataJson = objectMapper.writeValueAsString(result);
                // Check if the chartDataJson variable is not null before trying to render the template
                if (chartDataJson != null) {
                    return sentiment.data("sentimentChartData", chartDataJson)
                            .data("s_book", book)
                            .data("s_start_chapter", startChapter.get())
                            .data("s_start_verse", startVerse.get())
                            .data("s_end_chapter", endChapter.get())
                            .data("s_end_verse", endVerse.get()).render();
                } else {
                    return "No data found for this verse range.";
                }
            } catch (JsonProcessingException e) {
                return sentiment.data("sentimentChartError", e.getMessage()).render();
             }
        }
        // For the initial visit to /sentiment, render the page without data
        return sentiment.data("sentimentChartData", null).render();
    }
}