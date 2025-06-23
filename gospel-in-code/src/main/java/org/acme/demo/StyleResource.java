// src/main/java/org/acme/StyleResource.java
package org.acme.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.acme.ReadabilityUtil;
import org.acme.Verse;

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

@Path("/style")
public class StyleResource {

    @Inject
    Template style; // Injects style.html

    @Inject
    ObjectMapper objectMapper;

    // Records for a structured response
    public record StyleData(List<String> labels, List<Dataset> datasets) {
    }

    public record Dataset(String label, List<Double> data) {
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String get(@QueryParam("book") Optional<String> bookOptional) {
        String book = bookOptional.orElse("John"); // Default to 'John' if not provided

        List<String> translations = Verse.find("select distinct translation from Verse").project(String.class).list();
        List<Dataset> datasets = new ArrayList<>();

        Log.info(datasets.size() + " datasets found for translations: " + translations);

        for (String translation : translations) {
            String fullText = Verse.<Verse>find("translation = ?1 and book = ?2", translation, book)
                    .stream()
                    .map(v -> v.text)
                    .collect(Collectors.joining(" "));

            if (fullText.isBlank()) {
            Log.warn("No text found for translation: " + translation + " in book: " + book);
                continue;
            }

            String[] words = fullText.toLowerCase().replaceAll("[^a-z\\s]", "").split("\\s+");
            long sentenceCount = ReadabilityUtil.countSentences(fullText);

            // 1. Lexical Diversity
            double lexicalDiversity = (double) new HashSet<>(Arrays.asList(words)).size() / words.length;

            // 2. Sentence Complexity
            double avgSentenceLength = (double) words.length / sentenceCount;

            // 3. Flesch-Kincaid Score (using our direct implementation)
            double fkScore = ReadabilityUtil.calculateFleschKincaidReadingEase(fullText);

            // Normalize scores for better radar chart visualization
            List<Double> data = List.of(
                    lexicalDiversity,
                    Math.min(1.0, avgSentenceLength / 30.0), // Cap complexity score at 1.0
                    Math.max(0.0, fkScore / 100.0) // Clamp score between 0 and 1
            );
            datasets.add(new Dataset(translation, data));
        }

        StyleData styleData = new StyleData(List.of("Lexical Diversity", "Sentence Complexity", "Readability (FK)"),
                datasets);

        try {
            String styleJson = objectMapper.writeValueAsString(styleData);
            return style.data("styleJson", styleJson)
                    .data("selectedBook", book).render();
        } catch (JsonProcessingException e) {
            return style.data("styleError", e.getMessage()).render();
        }
    }
}