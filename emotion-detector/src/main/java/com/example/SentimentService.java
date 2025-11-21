package com.example;

import java.util.Base64;
import java.util.logging.Logger;

import dev.langchain4j.data.image.Image;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SentimentService {

    private static final Logger LOG = Logger.getLogger(SentimentService.class.getName());

    @Inject
    EmotionAgent agent;

    public Uni<String> analyzeAsync(byte[] jpeg) {
        LOG.info("Starting sentiment analysis for " + jpeg.length + " byte image");
        return Uni.createFrom().item(() -> {
            try {
                LOG.info("Encoding image to base64...");
                String base64String = Base64.getEncoder().encodeToString(jpeg);
                LOG.info("Base64 length: " + base64String.length());

                // Log a sample of the base64 to verify it's image data, not metadata
                if (base64String.length() > 100) {
                    LOG.info("Base64 preview (first 100 chars): " + base64String.substring(0, 100));
                }

                LOG.info("Creating Image using builder...");
                Image langchainImage = Image.builder()
                        .base64Data(base64String)
                        .mimeType("image/jpeg")
                        .build();

                LOG.info("Calling agent.detect()...");
                String result = agent.detect(langchainImage);
                LOG.info("Agent returned: " + result);

                return result;
            } catch (Exception e) {
                LOG.severe("Error in sentiment analysis: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        });
    }
}