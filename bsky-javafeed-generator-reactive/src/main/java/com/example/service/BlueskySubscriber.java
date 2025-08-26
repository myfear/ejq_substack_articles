package com.example.service;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.example.model.PostEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnError;
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

@ApplicationScoped
@WebSocketClient(path = "")
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

            String description = "Jetstream2 US-East (Official)";

            LOG.infof("Attempting to connect to %s: %s", description, firehoseUrl);

            connector.baseUri(URI.create(firehoseUrl))
                    .connectAndAwait();

            LOG.info("Successfully connected to Jetstream WebSocket");
        } catch (Exception e) {
            LOG.errorf(e, "Failed to connect to Jetstream WebSocket: %s", e.getMessage());
            reconnectWithBackoff(1); // start at 1s
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

    @OnClose
    void closed(WebSocketClientConnection conn) {
        LOG.warn("WebSocket closed. Will attempt to reconnect.");
        reconnectWithBackoff(1); // start at 1s
    }

    @OnError
    void errored(WebSocketClientConnection conn, Throwable t) {
        LOG.warn("WebSocket error: " + t.getMessage() + " — will attempt to reconnect.", t);
        reconnectWithBackoff(1);
    }

    void reconnectWithBackoff(int seconds) {
        int delay = Math.min(seconds, 60); // cap at 60s
        // add jitter +/- 20%
        int jittered = (int) Math.max(1, delay * (0.8 + ThreadLocalRandom.current().nextDouble(0.4)));

        Uni.createFrom().voidItem()
                .onItem().delayIt().by(Duration.ofSeconds(jittered))
                .subscribe().with(
                        v -> {
                            try {
                                connector.baseUri(URI.create(firehoseUrl)).connectAndAwait();
                                LOG.info("Reconnected to Jetstream");
                            } catch (Exception e) {
                                LOG.warnf(e, "Reconnect failed; will retry with larger backoff");
                                reconnectWithBackoff(delay * 2); // exponential
                            }
                        },
                        err -> {
                            LOG.warn("Backoff timer failed: " + err.getMessage());
                            reconnectWithBackoff(delay * 2);
                        });
    }

    @OnTextMessage
    public Multi<Void> stream(Multi<String> incoming) {
        return incoming
                // Buffer small bursts. If your DB is slow, increase cautiously.
                .onOverflow().buffer(512)
                // Fail fast on huge spikes instead of OOM (alternative strategies below)
                .onOverflow().drop()
                // Process items one by one, preserving order
                .onItem().transformToUniAndConcatenate(json -> processJetstreamEvent(json));
    }

    @WithTransaction
    Uni<Void> processJetstreamEvent(String json) {
        try {
            // Parse JSON text into a tree for inspection
            JsonNode root = objectMapper.readTree(json);

            // Jetstream message format: {"kind": "commit", "commit": {...}, "did": "..."}
            if (!"commit".equals(root.path("kind").asText())) {
                return Uni.createFrom().voidItem(); // ignore non-commit events (e.g. 'identity' or 'account' updates)
            }

            JsonNode commit = root.path("commit");
            if (!"create".equals(commit.path("operation").asText()) ||
                    !"app.bsky.feed.post".equals(commit.path("collection").asText())) {
                return Uni.createFrom().voidItem(); // not a new post creation, ignore (could be likes, follows, etc.)
            }

            // Extract post text and creation timestamp
            String text = commit.path("record").path("text").asText("");
            String createdAtStr = commit.path("record").path("createdAt").asText("");

            // Check if the post has English language variants
            if (!hasEnglishLanguage(commit.path("record"))) {
                LOG.tracef("Skipping non-English post: %s", text.substring(0, Math.min(50, text.length())));
                return Uni.createFrom().voidItem();
            }

            // Check for Java-related hashtags using the existing extractHashtags method
            String hashtags = extractHashtags(text);
            boolean hasJavaHashtag = containsJavaRelatedHashtags(hashtags);

            // Distinguish tech vs. travel context for "#Java"
            if (text.isEmpty() || !hasJavaHashtag || !isTechRelatedPost(text)) {
                // It's a #Java mention likely about Java (island/coffee), skip indexing
                LOG.tracef("Skipping non-tech Java post: %s", text.substring(0, Math.min(50, text.length())));
                return Uni.createFrom().voidItem();
            }

            // Extract metadata
            OffsetDateTime createdAt;
            try {
                createdAt = OffsetDateTime.parse(createdAtStr);
            } catch (DateTimeParseException e) {
                LOG.warnf("Failed to parse creation date '%s' for post: %s", createdAtStr, e.getMessage());
                return Uni.createFrom().voidItem(); // Skip this post if we can't parse the date
            }

            int hour = createdAt.getHour();
            String frameworks = findFrameworks(text);
            String links = extractLinks(text);
            String language = detectLanguage(commit.path("record"), text);

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

            // Return the persistence operation to ensure it completes within the
            // transaction
            return post.persist()
                    .onItem().ignore().andContinueWithNull()
                    .onItem().invoke(() -> {
                        LOG.tracef("Indexed post %s (hour %d, frameworks: %s)", atUri, hour, frameworks);
                    })
                    .onFailure().invoke(failure -> {
                        LOG.errorf(failure, "Failed to persist post %s to database: %s", atUri, failure.getMessage());
                    });

        } catch (Exception e) {
            LOG.errorf(e, "Unexpected error processing Jetstream event: %s", e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }

    /**
     * Determines if a #Java post is tech-related or not.
     * Uses existing language detection methods for cleaner logic.
     */
    private boolean isTechRelatedPost(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        LOG.tracef("check if post is tech-related: " + text);

        String textLower = text.toLowerCase();

        // Check for travel/coffee context using existing Indonesian word detection
        if (containsIndonesianWords(textLower) ||
                textLower.contains("indonesia") ||
                textLower.contains("jakarta") ||
                textLower.contains("coffee") ||
                textLower.contains("island")) {
            return false; // mentions of Indonesia/coffee likely mean Java the place or coffee
        }

        // Check for tech context using existing framework detection
        String frameworks = findFrameworks(text);
        if (!frameworks.isEmpty()) {
            return true; // contains known Java frameworks
        }

        // Check for other tech indicators
        if (textLower.contains("jdk") || textLower.contains("programming") ||
                textLower.contains("code") || textLower.contains("developer") || textLower.contains("software")
                || textLower.contains("dev") || textLower.contains("tech")) {
            return true;
        }

        // Default: assume tech if none of the travel indicators were present
        return true;
    }

    // Find known Java-related frameworks or libraries mentioned in text
    private String findFrameworks(String text) {
        try {
            String[] techTerms = { "Quarkus", "Spring", "SpringBoot", "Jakarta", "Hibernate", "JDK", "JVM", "Helidon",
                    "JavaEE" };
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

    /**
     * Checks if the post has English language variants in the langs array.
     * Supports ISO 639-1 language codes and extended language tags.
     */
    private boolean hasEnglishLanguage(JsonNode record) {
        JsonNode langs = record.path("langs");

        // If no langs array, assume English (backward compatibility)
        if (langs.isMissingNode() || !langs.isArray()) {
            return true;
        }

        // Check if any language in the array is an English variant
        for (JsonNode lang : langs) {
            if (isEnglishVariant(lang.asText())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a language code represents an English variant.
     * Supports: en, en-US, en-GB, en-CA, en-AU, etc.
     */
    private boolean isEnglishVariant(String languageCode) {
        if (languageCode == null || languageCode.trim().isEmpty()) {
            return false;
        }

        String normalized = languageCode.trim().toLowerCase();
        return normalized.equals("en") || normalized.startsWith("en-");
    }

    /**
     * Detects the primary language from the langs array or falls back to content
     * analysis.
     */
    private String detectLanguage(JsonNode record, String text) {
        JsonNode langs = record.path("langs");

        // If langs array exists and has content, use the first language
        if (!langs.isMissingNode() && langs.isArray() && langs.size() > 0) {
            String primaryLang = langs.get(0).asText();
            if (!primaryLang.trim().isEmpty()) {
                return primaryLang;
            }
        }

        // Fallback: analyze text content for language hints
        return detectLanguageFromText(text);
    }

    /**
     * Checks if the extracted hashtags contain Java-related content.
     */
    private boolean containsJavaRelatedHashtags(String hashtags) {
        if (hashtags == null || hashtags.trim().isEmpty()) {
            return false;
        }

        String[] javaHashtags = { "#java", "#javadev", "#javaprogramming", "#spring", "#quarkus" };
        String normalizedHashtags = hashtags.toLowerCase();

        for (String hashtag : javaHashtags) {
            if (normalizedHashtags.contains(hashtag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Basic language detection from text content using common patterns.
     */
    private String detectLanguageFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "en"; // Default fallback
        }

        String normalized = text.toLowerCase();

        // Simple heuristics for common languages
        if (containsIndonesianWords(normalized)) {
            return "id";
        }
        if (containsJapaneseCharacters(normalized)) {
            return "ja";
        }
        if (containsKoreanCharacters(normalized)) {
            return "ko";
        }
        if (containsChineseCharacters(normalized)) {
            return "zh";
        }

        // Default to English if no other language indicators found
        return "en";
    }

    private boolean containsIndonesianWords(String text) {
        String[] indonesianWords = { "sebuah", "pulau", "indonesia", "jakarta" };
        for (String word : indonesianWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsJapaneseCharacters(String text) {
        return text.matches(".*[\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF].*");
    }

    private boolean containsKoreanCharacters(String text) {
        return text.matches(".*[\\uAC00-\\uD7AF].*");
    }

    private boolean containsChineseCharacters(String text) {
        return text.matches(".*[\\u4E00-\\u9FFF].*");
    }

}
