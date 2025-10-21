package com.example.service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;
import org.kohsuke.github.GHContent;

import com.example.github.AwesomePromptsClient;
import com.example.model.Prompt;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PromptRepository {

    private static final Logger LOG = Logger.getLogger(PromptRepository.class);
    private static final Path CACHE_DIR = Paths.get("target", "cache");

    @Inject
    AwesomePromptsClient github;
    @Inject
    PromptParser parser;
    @Inject
    ObjectMapper mapper;

    private final Map<String, Prompt> cache = new ConcurrentHashMap<>();
    private volatile boolean refreshing = false;

    @Startup
    void init() {
        try {
            Files.createDirectories(CACHE_DIR);
            loadFromCache();
            if (cache.isEmpty()) {
                LOG.info("No cached prompts found — fetching from GitHub...");
                refreshPrompts();
            } else {
                LOG.infof("Loaded %d prompts from local cache", cache.size());
            }
        } catch (Exception e) {
            LOG.error("Failed to initialize prompt cache", e);
            refreshPrompts();
        }
    }

    @Scheduled(every = "60m")
    public void refreshPrompts() {
        if (refreshing) {
            LOG.info("Refresh already in progress, skipping...");
            return;
        }
        
        refreshing = true;
        try {
            LOG.info("Refreshing prompts...");
            List<GHContent> files = github.fetchPromptFiles();
            int newCount = 0;
            int skipped = 0;

            for (GHContent file : files) {
                String id = generateIdFromPath(file.getPath());
                Path cachedFile = CACHE_DIR.resolve(id + ".json");

                if (Files.exists(cachedFile)) {
                    skipped++;
                    continue;
                }

                try {
                    String content = github.fetchFileContent(file.getPath());
                    Prompt prompt = parser.parseMarkdownFile(content, file.getPath());
                    cache.put(prompt.id(), prompt);
                    savePromptToCache(prompt);
                    newCount++;
                } catch (Exception ex) {
                    LOG.warnf("Failed to parse %s: %s", file.getPath(), ex.getMessage());
                }
            }

            LOG.infof("Prompt refresh complete. Added %d new prompts, skipped %d (already cached)", newCount, skipped);
            LOG.infof("Total prompts in memory: %d", cache.size());

        } catch (Exception e) {
            LOG.error("Failed to refresh prompts", e);
        } finally {
            refreshing = false;
        }
    }

    public List<Prompt> all() {
        return new ArrayList<>(cache.values());
    }

    public Prompt byId(String id) {
        Prompt prompt = cache.get(id);
        if (prompt != null)
            return prompt;

        // Try lazy load from file cache
        Path cachedFile = CACHE_DIR.resolve(id + ".json");
        if (Files.exists(cachedFile)) {
            try {
                prompt = mapper.readValue(cachedFile.toFile(), Prompt.class);
                cache.put(prompt.id(), prompt);
                return prompt;
            } catch (IOException e) {
                LOG.warnf("Failed to read cached file %s: %s", id, e.getMessage());
            }
        }
        return null;
    }

    public List<Prompt> search(String q, Prompt.PromptCategory c, List<String> tags) {
        return cache.values().stream()
                .filter(p -> q == null || p.title().toLowerCase().contains(q.toLowerCase()))
                .filter(p -> c == null || p.category() == c)
                .filter(p -> tags == null || tags.isEmpty() || p.tags().stream().anyMatch(tags::contains))
                .toList();
    }

    /* ---------------- Helper methods ---------------- */

    private void loadFromCache() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(CACHE_DIR, "*.json")) {
            for (Path file : stream) {
                try {
                    Prompt prompt = mapper.readValue(file.toFile(), Prompt.class);
                    cache.put(prompt.id(), prompt);
                } catch (Exception e) {
                    LOG.warnf("Skipping invalid cache file %s: %s", file.getFileName(), e.getMessage());
                }
            }
        } catch (NoSuchFileException e) {
            LOG.info("No existing cache directory");
        } catch (IOException e) {
            LOG.warn("Failed to read local prompt cache", e);
        }
    }

    private void savePromptToCache(Prompt prompt) {
        Path path = CACHE_DIR.resolve(prompt.id() + ".json");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), prompt);
        } catch (IOException e) {
            LOG.warnf("Failed to write cache file %s: %s", path, e.getMessage());
        }
    }

    private String generateIdFromPath(String path) {
        // Use the same ID generation logic as PromptParser
        return path.replaceAll("[^a-zA-Z0-9]", "-").toLowerCase();
    }
}
