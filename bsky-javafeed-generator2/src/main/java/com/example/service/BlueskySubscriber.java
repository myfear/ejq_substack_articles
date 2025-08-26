package com.example.service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.example.model.PostEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocketClient;
import io.quarkus.websockets.next.WebSocketClientConnection;
import io.quarkus.websockets.next.WebSocketConnector;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@WebSocketClient(path = "/subscribe")
public class BlueskySubscriber {

    @Inject
    WebSocketConnector<BlueskySubscriber> connector;

    private static final Logger LOG = Logger.getLogger(BlueskySubscriber.class);

    @ConfigProperty(name = "app.jetstream.uri")
    String firehoseUrl;

    @Inject
    ObjectMapper objectMapper;

    void onStart(@Observes StartupEvent ev) {
        try {
            // Based on official Jetstream documentation:
            // https://github.com/bluesky-social/jetstream
            // String firehoseUrl =
            // "wss://jetstream2.us-east.bsky.network/subscribe?wantedCollections=app.bsky.feed.post";
            String description = "Jetstream2 US-East (Official)";

            // Initialize WebSocket client with SSL options
            // WebSocketClientOptions options = new WebSocketClientOptions()
            // .setSsl(true)
            // .setVerifyHost(false)
            // .setTrustAll(true); // For testing - in production use proper SSL
            // verification

            // connectToFirehose(firehoseUrl, description);

            LOG.infof("Attempting to connect to %s: %s", description, firehoseUrl);

            connector.baseUri(URI.create(firehoseUrl))
                    .connectAndAwait();

            LOG.info("Successfully connected to Jetstream WebSocket");
        } catch (Exception e) {
            LOG.errorf(e, "Failed to connect to Jetstream WebSocket: %s", e.getMessage());
            // In a production environment, you might want to implement retry logic here
        }
    }

    @OnOpen
    void opened(WebSocketClientConnection conn) {
        try {
            LOG.info("Jetstream WebSocket opened");
        } catch (Exception e) {
            LOG.errorf(e, "Error in WebSocket open handler: %s", e.getMessage());
        }
    }

    @OnTextMessage
    public Multi<Void> stream(Multi<String> incoming) {
        return incoming
                // Buffer small bursts. If your DB is slow, increase cautiously.
                .onOverflow().buffer(512)
                // Fail fast on huge spikes instead of OOM (alternative strategies below)
                .onOverflow().drop()
                // Process items one by one, preserving order
                .onItem().transformToUniAndConcatenate(json -> handle(json));
    }

    private Uni<Void> handle(String json) {
        // Let the existing method do filtering, enrichment, persistence.
        return Uni.createFrom().voidItem()
                .invoke(() -> processJetstreamEvent(json))
                .onFailure()
                .invoke(throwable -> LOG.errorf(throwable, "Error handling JSON message: %s", throwable.getMessage()));
    }

    @Transactional
    void processJetstreamEvent(String json) {
        try {
            // Parse JSON text into a tree for inspection
            JsonNode root = objectMapper.readTree(json);

            // Jetstream message format: {"kind": "commit", "commit": {...}, "did": "..."}
            if (!"commit".equals(root.path("kind").asText())) {
                return; // ignore non-commit events (e.g. 'identity' or 'account' updates)
            }

            JsonNode commit = root.path("commit");
            if (!"create".equals(commit.path("operation").asText()) ||
                    !"app.bsky.feed.post".equals(commit.path("collection").asText())) {
                return; // not a new post creation, ignore (could be likes, follows, etc.)
            }

            // Extract post text and creation timestamp
            String text = commit.path("record").path("text").asText("");
            String createdAtStr = commit.path("record").path("createdAt").asText("");
            if (text.isEmpty() || !text.contains("#Java")) {
                return; // Skip if no text or does not contain #Java hashtag
            }

            // Distinguish tech vs. travel context for "#Java"
            if (!isTechRelatedPost(text)) {
                // It's a #Java mention likely about Java (island/coffee), skip indexing
                return;
            }

            // Extract metadata
            OffsetDateTime createdAt;
            try {
                createdAt = OffsetDateTime.parse(createdAtStr);
            } catch (DateTimeParseException e) {
                LOG.warnf("Failed to parse creation date '%s' for post: %s", createdAtStr, e.getMessage());
                return; // Skip this post if we can't parse the date
            }

            int hour = createdAt.getHour();
            String frameworks = findFrameworks(text);
            String hashtags = extractHashtags(text);
            String links = extractLinks(text);
            String language = detectLanguage(text);

            // Construct the AT URI for the post: "at://{did}/app.bsky.feed.post/{rkey}"
            String userDid = root.path("did").asText();
            String rkey = commit.path("rkey").asText();
            String atUri = "at://" + userDid + "/app.bsky.feed.post/" + rkey;

            // Persist to database via Panache entity
            PostEntity post = new PostEntity();
            post.uri = atUri;
            post.text = text;
            post.createdAt = createdAt;
            post.hourOfDay = hour;
            post.frameworks = frameworks;
            post.hashtags = hashtags;
            post.links = links;
            post.language = language;

            try {
                post.persist(); // Panache will insert the record (within the @Transactional context)
                LOG.infof("Indexed post %s (hour %d, frameworks: %s)", atUri, hour, frameworks);
            } catch (Exception e) {
                LOG.errorf(e, "Failed to persist post %s to database: %s", atUri, e.getMessage());
                throw e; // Re-throw to trigger transaction rollback
            }

        } catch (JsonProcessingException e) {
            LOG.errorf(e, "Failed to parse JSON message: %s", e.getMessage());
        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error processing Jetstream event: %s", e.getMessage());
        }
    }

    // Determine if a #Java post is tech-related or not
    private boolean isTechRelatedPost(String text) {
        try {
            String textLower = text.toLowerCase();
            // Keywords indicating a travel/coffee context for "Java"
            if (textLower.contains("indonesia") || textLower.contains("jakarta")
                    || textLower.contains("coffee") || textLower.contains("island")) {
                return false; // mentions of Indonesia/coffee likely mean Java the place or coffee
            }
            // Keywords that strongly indicate tech context
            if (textLower.contains("spring") || textLower.contains("quarkus")
                    || textLower.contains("jdk") || textLower.contains("programming")) {
                return true;
            }
            // (Basic language hint: if contains typical Indonesian words, you could flag as
            // non-tech too)
            if (textLower.matches(".*\\bsebuah\\b.*") || textLower.matches(".*\\bpulau\\b.*")) {
                // e.g. Indonesian words "sebuah" (a/an), "pulau" (island)
                return false;
            }
            // Default: assume tech if none of the travel indicators were present
            return true;
        } catch (Exception e) {
            LOG.errorf(e, "Error determining if post is tech-related: %s", e.getMessage());
            return true; // Default to tech-related on error
        }
    }

    // Find known Java-related frameworks or libraries mentioned in text
    private String findFrameworks(String text) {
        try {
            String[] techTerms = { "Spring", "Quarkus", "Jakarta", "Hibernate", "JDK", "JVM" };
            StringBuilder found = new StringBuilder();
            for (String term : techTerms) {
                if (text.contains(term)) {
                    if (found.length() > 0)
                        found.append(",");
                    found.append(term);
                }
            }
            return found.toString();
        } catch (Exception e) {
            LOG.errorf(e, "Error finding frameworks in text: %s", e.getMessage());
            return "";
        }
    }

    // Extract all hashtags (e.g. #Java, #Quarkus) from text
    private String extractHashtags(String text) {
        try {
            Matcher m = Pattern.compile("#\\w+").matcher(text);
            StringBuilder tags = new StringBuilder();
            while (m.find()) {
                if (tags.length() > 0)
                    tags.append(",");
                tags.append(m.group());
            }
            return tags.toString();
        } catch (Exception e) {
            LOG.errorf(e, "Error extracting hashtags from text: %s", e.getMessage());
            return "";
        }
    }

    // Extract all URLs from text (simple regex for http/https links)
    private String extractLinks(String text) {
        try {
            Matcher m = Pattern.compile("(https?://\\S+)").matcher(text);
            StringBuilder links = new StringBuilder();
            while (m.find()) {
                if (links.length() > 0)
                    links.append(",");
                links.append(m.group());
            }
            return links.toString();
        } catch (Exception e) {
            LOG.errorf(e, "Error extracting links from text: %s", e.getMessage());
            return "";
        }
    }

    // Very basic language detection (placeholder for a real NLP library)
    private String detectLanguage(String text) {
        try {
            // For demo: if contains likely English stopwords vs. Indonesian words, etc.
            // Here we'll just default to "en" for simplicity.
            return "en";
        } catch (Exception e) {
            LOG.errorf(e, "Error detecting language: %s", e.getMessage());
            return "en"; // Default to English on error
        }
    }

}
