package com.example.service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

import com.example.model.PostEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocketClient;
import io.vertx.core.http.WebSocketClientOptions;
import io.vertx.core.http.WebSocketConnectOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class BlueskySubscriber {

    private static final Logger LOG = Logger.getLogger(BlueskySubscriber.class);

    @Inject
    Vertx vertx;

    private WebSocketClient wsClient;

    void onStart(@Observes StartupEvent ev) {
        // Based on official Jetstream documentation:
        // https://github.com/bluesky-social/jetstream
        String firehoseUrl = "wss://jetstream2.us-east.bsky.network/subscribe?wantedCollections=app.bsky.feed.post";
        String description = "Jetstream2 US-East (Official)";

        // Initialize WebSocket client with SSL options
        WebSocketClientOptions options = new WebSocketClientOptions()
                .setSsl(true)
                .setVerifyHost(false)
                .setTrustAll(true); // For testing - in production use proper SSL verification

        wsClient = vertx.createWebSocketClient(options);

        connectToFirehose(firehoseUrl, description);
    }

    private void connectToFirehose(String url, String description) {
        LOG.infof("Attempting to connect to %s: %s", description, url);

        try {
            URI uri = URI.create(url);
            WebSocketConnectOptions connectOptions = new WebSocketConnectOptions()
                    .setHost(uri.getHost())
                    .setPort(uri.getPort() == -1 ? 443 : uri.getPort())
                    .setURI(uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : ""))
                    .setSsl(true);

            wsClient.connect(connectOptions)
                    .onSuccess(ws -> {
                        LOG.infof("Successfully connected to %s: %s", description, url);

                        // Set up message handlers - dispatch to worker thread for transaction support
                        ws.textMessageHandler(message -> {
                            vertx.executeBlocking(() -> {
                                handleTextMessage(message);
                                return null;
                            }, false);
                        });

                        // Set up error and close handlers
                        ws.exceptionHandler(error -> {
                            LOG.errorf("WebSocket error on %s: %s", description, error.getMessage());
                            // Try to reconnect after a delay
                            vertx.setTimer(5000, timer -> connectToFirehose(url, description));
                        });

                        ws.closeHandler(closeReason -> {
                            LOG.warnf("WebSocket closed on %s", description);
                            // Try to reconnect after a delay
                            vertx.setTimer(5000, timer -> connectToFirehose(url, description));
                        });

                        LOG.infof("🎉 Listening for real-time Bluesky posts...");
                    })
                    .onFailure(error -> {
                        LOG.errorf("Failed to connect to %s (%s): %s", description, url, error.getMessage());
                        // Try to reconnect after a delay
                        vertx.setTimer(10000, timer -> connectToFirehose(url, description));
                    });
        } catch (Exception e) {
            LOG.errorf("Error parsing URL %s: %s", url, e.getMessage());
            // Try to reconnect after a delay
            vertx.setTimer(10000, timer -> connectToFirehose(url, description));
        }
    }

    private void handleTextMessage(String message) {
        // This method is invoked for each incoming JSON message from Jetstream
        try {
            processJetstreamEvent(message);
        } catch (Exception ex) {
            LOG.error("Error processing Jetstream event: ", ex);
        }
    }

    @Transactional
    void processJetstreamEvent(String json) throws Exception {
        // Parse JSON text into a tree for inspection
        JsonNode root = new ObjectMapper().readTree(json);

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
        OffsetDateTime createdAt = OffsetDateTime.parse(createdAtStr);
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
        post.persist(); // Panache will insert the record (within the @Transactional context)

        LOG.infof("Indexed post %s (hour %d, frameworks: %s)", atUri, hour, frameworks);
    }

    // Determine if a #Java post is tech-related or not
    private boolean isTechRelatedPost(String text) {
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
    }

    // Find known Java-related frameworks or libraries mentioned in text
    private String findFrameworks(String text) {
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
    }

    // Extract all hashtags (e.g. #Java, #Quarkus) from text
    private String extractHashtags(String text) {
        Matcher m = Pattern.compile("#\\w+").matcher(text);
        StringBuilder tags = new StringBuilder();
        while (m.find()) {
            if (tags.length() > 0)
                tags.append(",");
            tags.append(m.group());
        }
        return tags.toString();
    }

    // Extract all URLs from text (simple regex for http/https links)
    private String extractLinks(String text) {
        Matcher m = Pattern.compile("(https?://\\S+)").matcher(text);
        StringBuilder links = new StringBuilder();
        while (m.find()) {
            if (links.length() > 0)
                links.append(",");
            links.append(m.group());
        }
        return links.toString();
    }

    // Very basic language detection (placeholder for a real NLP library)
    private String detectLanguage(String text) {
        // For demo: if contains likely English stopwords vs. Indonesian words, etc.
        // Here we'll just default to "en" for simplicity.
        return "en";
    }

}
