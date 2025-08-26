package com.example.service;

import java.net.URI;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.model.PostEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.logging.Log;
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

/**
 * BlueskySubscriber is a reactive service that connects to the Bluesky Jetstream WebSocket
 * to receive real-time posts and filter them for Java-related content.
 * 
 * <p>This service implements the following functionality:</p>
 * <ul>
 *   <li>WebSocket connection management with automatic reconnection</li>
 *   <li>Real-time filtering of posts for Java-related hashtags and content</li>
 *   <li>Language detection and filtering (English variants only)</li>
 *   <li>Tech context analysis to distinguish programming Java from geographic Java</li>
 *   <li>Framework detection for Spring, Quarkus, and other Java technologies</li>
 *   <li>Persistent storage of relevant posts to the database</li>
 * </ul>
 * 
 * <p>The service connects to the Bluesky Jetstream firehose and processes incoming
 * posts in real-time, applying multiple filters to ensure only relevant Java programming
 * content is indexed.</p>
 * 
 * @author Bluesky Java Feed Generator
 * @version 1.0
 * @since 1.0
 */
@ApplicationScoped
@WebSocketClient(path = "")
public class BlueskySubscriber {

    /**
     * WebSocket connector for establishing and managing connections to the Bluesky Jetstream.
     */
    @Inject
    WebSocketConnector<BlueskySubscriber> connector;

    /**
     * Configuration property for the Bluesky Jetstream WebSocket URI.
     * Injected from application.properties.
     */
    @ConfigProperty(name = "app.jetstream.uri")
    String firehoseUrl;

    /**
     * Jackson ObjectMapper for JSON parsing and processing.
     */
    @Inject
    ObjectMapper objectMapper;

    /**
     * Lifecycle method called when the application starts.
     * Establishes the initial WebSocket connection to the Bluesky Jetstream.
     * 
     * @param ev The startup event that triggered this method
     */
    void onStart(@Observes StartupEvent ev) {
        try {

            String description = "Jetstream2 US-East (Official)";

            Log.infof("Attempting to connect to %s: %s", description, firehoseUrl);

            connector.baseUri(URI.create(firehoseUrl))
                    .connectAndAwait();

            Log.info("Successfully connected to Jetstream WebSocket");
        } catch (Exception e) {
            Log.errorf(e, "Failed to connect to Jetstream WebSocket: %s", e.getMessage());
            reconnectWithBackoff(1); // start at 1s
        }
    }

    /**
     * WebSocket event handler called when the connection is successfully opened.
     * Logs the successful connection establishment.
     * 
     * @param conn The WebSocket client connection that was opened
     */
    @OnOpen
    void opened(WebSocketClientConnection conn) {
        try {
            Log.info("Jetstream WebSocket opened");
        } catch (Exception e) {
            Log.errorf(e, "Error in WebSocket open handler: %s", e.getMessage());
        }
    }

    /**
     * WebSocket event handler called when the connection is closed.
     * Initiates reconnection with exponential backoff.
     * 
     * @param conn The WebSocket client connection that was closed
     */
    @OnClose
    void closed(WebSocketClientConnection conn) {
        Log.warn("WebSocket closed. Will attempt to reconnect.");
        reconnectWithBackoff(1); // start at 1s
    }

    /**
     * WebSocket event handler called when an error occurs on the connection.
     * Logs the error and initiates reconnection with exponential backoff.
     * 
     * @param conn The WebSocket client connection that encountered an error
     * @param t The throwable that represents the error
     */
    @OnError
    void errored(WebSocketClientConnection conn, Throwable t) {
        Log.warn("WebSocket error: " + t.getMessage() + " — will attempt to reconnect.", t);
        reconnectWithBackoff(1);
    }

    /**
     * Implements exponential backoff reconnection strategy for the WebSocket connection.
     * Uses jittered delays to prevent thundering herd problems during reconnection.
     * 
     * @param seconds The initial delay in seconds before attempting reconnection
     */
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
                                Log.info("Reconnected to Jetstream");
                            } catch (Exception e) {
                                Log.warnf(e, "Reconnect failed; will retry with larger backoff");
                                reconnectWithBackoff(delay * 2); // exponential
                            }
                        },
                        err -> {
                            Log.warn("Backoff timer failed: " + err.getMessage());
                            reconnectWithBackoff(delay * 2);
                        });
    }

    /**
     * WebSocket message handler that processes incoming JSON messages from the Bluesky Jetstream.
     * Implements reactive stream processing with overflow protection and ordered processing.
     * 
     * <p>The method applies the following processing pipeline:</p>
     * <ul>
     *   <li>Buffers small bursts of messages (512 items)</li>
     *   <li>Drops messages on overflow to prevent OOM</li>
     *   <li>Processes messages sequentially to preserve order</li>
     *   <li>Transforms each message to a database operation</li>
     * </ul>
     * 
     * @param incoming A Multi stream of JSON strings from the WebSocket
     * @return A Multi stream of Void items representing the processing results
     */
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

    /**
     * Processes a single Jetstream event from the Bluesky firehose.
     * Applies multiple filters to determine if the post should be indexed:
     * <ul>
     *   <li>Event type validation (commit events only)</li>
     *   <li>Post creation validation (new posts only)</li>
     *   <li>Language filtering (English variants only)</li>
     *   <li>Java hashtag detection</li>
     *   <li>Tech context analysis</li>
     * </ul>
     * 
     * <p>If all filters pass, the post is persisted to the database with extracted metadata.</p>
     * 
     * @param json The JSON string representing the Jetstream event
     * @return A Uni that completes when the processing is finished
     */
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
                Log.tracef("Skipping non-English post: %s", text.substring(0, Math.min(50, text.length())));
                return Uni.createFrom().voidItem();
            }

            // Check for Java-related hashtags using the existing extractHashtags method
            String hashtags = extractHashtags(text);
            boolean hasJavaHashtag = containsJavaRelatedHashtags(hashtags);

            // Distinguish tech vs. travel context for "#Java"
            if (text.isEmpty() || !hasJavaHashtag || !isTechRelatedPost(text)) {
                // It's a #Java mention likely about Java (island/coffee), skip indexing
                Log.tracef("Skipping non-tech Java post: %s", text.substring(0, Math.min(50, text.length())));
                return Uni.createFrom().voidItem();
            }

            // Extract metadata
            OffsetDateTime createdAt;
            try {
                createdAt = OffsetDateTime.parse(createdAtStr);
            } catch (DateTimeParseException e) {
                Log.warnf("Failed to parse creation date '%s' for post: %s", createdAtStr, e.getMessage());
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
                        Log.tracef("Indexed post %s (hour %d, frameworks: %s)", atUri, hour, frameworks);
                    })
                    .onFailure().invoke(failure -> {
                        Log.errorf(failure, "Failed to persist post %s to database: %s", atUri, failure.getMessage());
                    });

        } catch (Exception e) {
            Log.errorf(e, "Unexpected error processing Jetstream event: %s", e.getMessage());
            return Uni.createFrom().failure(e);
        }
    }

    /**
     * Determines if a #Java post is tech-related or not.
     * Uses existing language detection methods for cleaner logic.
     * 
     * <p>This method applies multiple heuristics to distinguish between:</p>
     * <ul>
     *   <li><strong>Tech context:</strong> Programming, development, frameworks</li>
     *   <li><strong>Travel context:</strong> Indonesia, coffee, geographic references</li>
     * </ul>
     * 
     * <p>The method uses existing utility methods for Indonesian word detection
     * and framework identification to make accurate determinations.</p>
     * 
     * @param text The post text to analyze
     * @return true if the post is tech-related, false if it's travel/coffee related
     */
    private boolean isTechRelatedPost(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        Log.tracef("check if post is tech-related: " + text);

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

    /**
     * Identifies Java-related frameworks and libraries mentioned in the post text.
     * Searches for common Java ecosystem technologies and returns them as a comma-separated string.
     * 
     * <p>Supported frameworks include:</p>
     * <ul>
     *   <li>Application servers: Quarkus, Spring, SpringBoot, Helidon</li>
     *   <li>Persistence: Hibernate, Jakarta</li>
     *   <li>Runtime: JDK, JVM</li>
     *   <li>Standards: JavaEE</li>
     * </ul>
     * 
     * @param text The post text to search for framework mentions
     * @return Comma-separated string of found frameworks, or empty string if none found
     */
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
            Log.errorf(e, "Error finding frameworks in text: %s", e.getMessage());
            return "";
        }
    }

    /**
     * Extracts all hashtags from the post text using regex pattern matching.
     * 
     * <p>This method finds hashtags that match the pattern #word and returns them
     * as a comma-separated string. Common examples include #Java, #Quarkus, #Spring.</p>
     * 
     * @param text The post text to extract hashtags from
     * @return Comma-separated string of found hashtags, or empty string if none found
     */
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
            Log.errorf(e, "Error extracting hashtags from text: %s", e.getMessage());
            return "";
        }
    }

    /**
     * Extracts all HTTP/HTTPS URLs from the post text using regex pattern matching.
     * 
     * <p>This method finds URLs that start with http:// or https:// and returns them
     * as a comma-separated string. Useful for identifying linked resources, documentation,
     * or code repositories mentioned in posts.</p>
     * 
     * @param text The post text to extract URLs from
     * @return Comma-separated string of found URLs, or empty string if none found
     */
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
            Log.errorf(e, "Error extracting links from text: %s", e.getMessage());
            return "";
        }
    }

    /**
     * Checks if the post has English language variants in the langs array.
     * Supports ISO 639-1 language codes and extended language tags.
     * 
     * <p>This method examines the "langs" field in the post record to determine
     * if the content is available in English. It supports various English variants:</p>
     * <ul>
     *   <li>Basic codes: "en"</li>
     *   <li>Regional variants: "en-US", "en-GB", "en-CA", "en-AU"</li>
     * </ul>
     * 
     * <p>For backward compatibility, posts without a "langs" field are assumed
     * to be in English.</p>
     * 
     * @param record The JSON record node containing the post metadata
     * @return true if the post has English language variants, false otherwise
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
     * 
     * <p>This method validates language codes according to ISO 639-1 standards
     * and extended language tag formats. It recognizes both basic English codes
     * and regional variants.</p>
     * 
     * @param languageCode The language code to validate
     * @return true if the code represents an English variant, false otherwise
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
     * 
     * <p>This method implements a two-tier language detection strategy:</p>
     * <ol>
     *   <li><strong>Primary:</strong> Uses the "langs" array from the post metadata</li>
     *   <li><strong>Fallback:</strong> Analyzes text content for language indicators</li>
     * </ol>
     * 
     * <p>The method prioritizes explicit language declarations over content analysis
     * to ensure accurate language identification.</p>
     * 
     * @param record The JSON record node containing the post metadata
     * @param text The post text content for fallback analysis
     * @return The detected language code (e.g., "en", "id", "ja", "ko", "zh")
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
     * 
     * <p>This method validates whether the provided hashtags contain any
     * Java-related programming hashtags. It searches for common Java ecosystem
     * hashtags in a case-insensitive manner.</p>
     * 
     * <p>Supported hashtags include:</p>
     * <ul>
     *   <li>#java - General Java programming</li>
     *   <li>#javadev - Java development</li>
     *   <li>#javaprogramming - Java programming</li>
     *   <li>#spring - Spring Framework</li>
     *   <li>#quarkus - Quarkus framework</li>
     * </ul>
     * 
     * @param hashtags Comma-separated string of hashtags to check
     * @return true if Java-related hashtags are found, false otherwise
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
     * 
     * <p>This method implements heuristic-based language detection by analyzing
     * text content for language-specific patterns, words, and characters.
     * It serves as a fallback when explicit language metadata is unavailable.</p>
     * 
     * <p>Supported languages and detection methods:</p>
     * <ul>
     *   <li><strong>Indonesian:</strong> Common Indonesian words (sebuah, pulau, etc.)</li>
     *   <li><strong>Japanese:</strong> Hiragana, Katakana, and Kanji characters</li>
     *   <li><strong>Korean:</strong> Hangul characters</li>
     *   <li><strong>Chinese:</strong> Simplified and Traditional Chinese characters</li>
     *   <li><strong>English:</strong> Default fallback when no other indicators found</li>
     * </ul>
     * 
     * @param text The text content to analyze for language indicators
     * @return The detected language code (e.g., "en", "id", "ja", "ko", "zh")
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

    /**
     * Checks if the text contains common Indonesian words that indicate non-tech context.
     * 
     * <p>This method helps distinguish between Java programming content and
     * content about Java (the Indonesian island). It searches for words commonly
     * used in Indonesian language posts about travel, geography, or coffee.</p>
     * 
     * @param text The text to search for Indonesian words
     * @return true if Indonesian words are found, false otherwise
     */
    private boolean containsIndonesianWords(String text) {
        String[] indonesianWords = { "sebuah", "pulau", "indonesia", "jakarta" };
        for (String word : indonesianWords) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects Japanese characters in the text using Unicode ranges.
     * 
     * <p>This method searches for Japanese writing system characters including:</p>
     * <ul>
     *   <li>Hiragana: \u3040-\u309F</li>
     *   <li>Katakana: \u30A0-\u30FF</li>
     *   <li>Kanji: \u4E00-\u9FAF</li>
     * </ul>
     * 
     * @param text The text to search for Japanese characters
     * @return true if Japanese characters are found, false otherwise
     */
    private boolean containsJapaneseCharacters(String text) {
        return text.matches(".*[\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF].*");
    }

    /**
     * Detects Korean characters in the text using Unicode ranges.
     * 
     * <p>This method searches for Korean Hangul characters in the Unicode range \uAC00-\uD7AF.</p>
     * 
     * @param text The text to search for Korean characters
     * @return true if Korean characters are found, false otherwise
     */
    private boolean containsKoreanCharacters(String text) {
        return text.matches(".*[\\uAC00-\\uD7AF].*");
    }

    /**
     * Detects Chinese characters in the text using Unicode ranges.
     * 
     * <p>This method searches for Chinese characters (both Simplified and Traditional)
     * in the Unicode range \u4E00-\u9FFF.</p>
     * 
     * @param text The text to search for Chinese characters
     * @return true if Chinese characters are found, false otherwise
     */
    private boolean containsChineseCharacters(String text) {
        return text.matches(".*[\\u4E00-\\u9FFF].*");
    }

}
