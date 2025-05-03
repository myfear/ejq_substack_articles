package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/sentiment")
public class SentimentResource {

    private static final Logger log = LoggerFactory.getLogger(SentimentResource.class);

    @Inject
    SentimentAnalyzer analyzer;

    @Inject
    @ConfigProperty(name = "quarkus.langchain4j.ollama.chat-model.model-id")
    String modelId;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String analyzeSentiment(@QueryParam("text") String text) {
        if (text == null || text.isBlank()) {
            log.warn("Empty text received for analysis.");
            return "Please provide text using the 'text' query parameter. Example: /sentiment?text=I+love+Quarkus!";
        }

        log.info("Analyzing text: '{}'", text);
        try {
            Sentiment sentiment = analyzer.classifySentiment(text);
            return String.format("Analyzed Text: '%s'\nPredicted Sentiment: %s\n(Model: Ollama/%s)",
                    text, sentiment, modelId);
        } catch (Exception e) {
            log.error("Sentiment analysis failed", e);
            return "Error during analysis. See server logs.";
        }
    }
}
