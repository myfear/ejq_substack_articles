package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.CosineSimilarity;

// Qute imports
import io.quarkus.qute.Template;

import java.util.Map; // Using Map to pass data
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jboss.logging.Logger;
import java.util.HashMap;

@Path("/")
public class SimilarityResource {

    private static final Logger log = Logger.getLogger(SimilarityResource.class); // Optional logging

    @Inject
    EmbeddingModel embeddingModel;

    @Inject
    Template similarity;

    @GET
    @Path("/similarity")
    @Produces(MediaType.TEXT_HTML) 
    public String get(
            @QueryParam("text1") String text1,
            @QueryParam("text2") String text2) {

        if (isBlank(text1) || isBlank(text2)) {
            log.warn("Missing input parameter for similarity check.");
            return similarity.data("error", "Please provide both text1 and text2 parameters.").render();
        }

        try {
            log.infof("Calculating similarity for text1: '%s' and text2: '%s'", text1, text2);
            // --- Embedding Generation ---
            Response<Embedding> embedding1 = embeddingModel.embed(text1);
            Response<Embedding> embedding2 = embeddingModel.embed(text2);
            float[] vector1 = embedding1.content().vector();
            float[] vector2 = embedding2.content().vector();

            // --- Calculations ---
            double cosSimilarity = CosineSimilarity.between(embedding1.content(), embedding2.content());
            float[] normVec1 = normalize(vector1);
            float[] normVec2 = normalize(vector2);
            double eucDistance = euclideanDistance(normVec1, normVec2);
            int dimensions = (vector1 != null) ? vector1.length : 0;

            // --- Prepare data for the template ---
            double clampedCosSimilarity = Math.max(0.0, Math.min(1.0, cosSimilarity));
            int similarityPercentage = (int) (clampedCosSimilarity * 100);

            // Format numbers to 4 decimal places
            String formattedCosSimilarity = String.format("%.4f", cosSimilarity);
            String formattedEucDistance = String.format("%.4f", eucDistance);

            // Format partial vectors for display (show first 10 dimensions)
            String vec1StartStr = formatVector(vector1, 10);
            String vec2StartStr = formatVector(vector2, 10);

            // Use a Map to pass data to the template
            Map<String, Object> data = new HashMap<>();
            data.put("text1", text1);
            data.put("text2", text2);
            data.put("dimensions", dimensions);
            data.put("cosSimilarity", formattedCosSimilarity);
            data.put("similarityPercentage", similarityPercentage);
            data.put("eucDistance", formattedEucDistance);
            data.put("vector1Start", vec1StartStr);
            data.put("vector2Start", vec2StartStr);
            data.put("error", null);

            return similarity.data(data).render();

        } catch (Exception e) {
            log.error("Error calculating similarity", e);
            return similarity.data("error", "An error occurred processing the request: " + e.getMessage()).render();
        }
    }

    // --- Helper Methods ---

    private boolean isBlank(String s) { return s == null || s.isBlank(); }

    // Helper to format the start of a vector for display
    private String formatVector(float[] vector, int count) {
         if (vector == null) return "[]";
         int displayCount = Math.min(count, vector.length);
         return IntStream.range(0, displayCount)
                       .mapToObj(i -> String.format("%.6f", vector[i]))
                       .collect(Collectors.joining(", ", "[", (vector.length > displayCount ? ", ...]" : "]")));
    }

    private float[] normalize(float[] vector) {
         if (vector == null || vector.length == 0) return new float[0];
         double mag = magnitude(vector);
         // Avoid division by zero or NaN for zero vectors
         if (mag == 0 || Double.isNaN(mag)) return vector.clone();
         float[] normalizedVector = new float[vector.length];
         for (int i = 0; i < vector.length; i++) {
             normalizedVector[i] = (float) (vector[i] / mag);
         }
         return normalizedVector;
     }

     private double magnitude(float[] vector) {
         if (vector == null) return 0;
         double sumOfSquares = 0;
         for (float f : vector) {
             sumOfSquares += f * f;
         }
         return Math.sqrt(sumOfSquares);
     }

     private double euclideanDistance(float[] vectorA, float[] vectorB) {
         // Check for null or dimension mismatch before proceeding
         if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
             // log.warn("Cannot calculate Euclidean distance due to vector mismatch or null vectors."); // Optional
             return Double.NaN; // Return NaN for invalid input
         }
         double sumOfSquares = 0;
         for (int i = 0; i < vectorA.length; i++) {
             sumOfSquares += Math.pow(vectorA[i] - vectorB[i], 2);
         }
         // The distance is the square root of the sum of squares
         return Math.sqrt(sumOfSquares);
     }
}