package com.happyplace.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SentimentService {

    @Inject
    AIService aiService; // To extract keywords

    // Store unique keywords, maintaining insertion order for potential pruning
    private final Set<String> likedKeywords = Collections.synchronizedSet(new LinkedHashSet<>());
    private final Set<String> dislikedKeywords = Collections.synchronizedSet(new LinkedHashSet<>());
    private static final int MAX_KEYWORDS_PER_CATEGORY = 25; // Max keywords to remember per category

    public Uni<Void> recordLike(String postText) {
        if (postText == null || postText.isBlank())
            return Uni.createFrom().voidItem();

        return aiService.extractKeywordsFromText(postText)
                .collect().asList()
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .invoke(tokens -> {
                    String keywordString = tokens.stream().collect(Collectors.joining());
                    if (keywordString != null && !keywordString.isBlank()) {
                        List<String> keywords = Arrays.asList(keywordString.toLowerCase().split(",\\s*"));
                        synchronized (likedKeywords) {
                            keywords.forEach(keyword -> {
                                likedKeywords.add(keyword.trim());
                                dislikedKeywords.remove(keyword.trim());
                            });
                            pruneKeywords(likedKeywords);
                        }
                        System.out.println("Extracted liked keywords: " + keywords);
                    }
                })
                .replaceWithVoid();
    }

    public Uni<Void> recordDislike(String postText) {
        if (postText == null || postText.isBlank())
            return Uni.createFrom().voidItem();

        return aiService.extractKeywordsFromText(postText)
                .collect().asList()
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .invoke(tokens -> {
                    String keywordString = tokens.stream().collect(Collectors.joining());
                    if (keywordString != null && !keywordString.isBlank()) {
                        List<String> keywords = Arrays.asList(keywordString.toLowerCase().split(",\\s*"));
                        synchronized (dislikedKeywords) {
                            keywords.forEach(keyword -> {
                                dislikedKeywords.add(keyword.trim());
                                likedKeywords.remove(keyword.trim());
                            });
                            pruneKeywords(dislikedKeywords);
                        }
                        System.out.println("Extracted disliked keywords: " + keywords);
                    }
                })
                .replaceWithVoid();
    }

    private void pruneKeywords(Set<String> keywordSet) {
        // Prune oldest keywords if the set grows too large
        while (keywordSet.size() > MAX_KEYWORDS_PER_CATEGORY) {
            // LinkedHashSet maintains insertion order, so iterator().next() gives the
            // oldest
            if (keywordSet.iterator().hasNext()) {
                String oldestKeyword = keywordSet.iterator().next();
                keywordSet.remove(oldestKeyword);
                System.out.println("Pruned keyword: " + oldestKeyword);
            } else {
                break; // Should not happen if size > 0
            }
        }
    }

    public String getAggregatedPreferences() {
        StringBuilder preferences = new StringBuilder();
        synchronized (likedKeywords) {
            if (!likedKeywords.isEmpty()) {
                preferences.append("User LIKES content related to themes/keywords such as: '")
                        .append(likedKeywords.stream().limit(10).collect(Collectors.joining("', '"))) // Limit for
                                                                                                      // prompt length
                        .append("'. ");
            }
        }
        synchronized (dislikedKeywords) {
            if (!dislikedKeywords.isEmpty()) {
                preferences.append("User DISLIKES content related to themes/keywords such as: '")
                        .append(dislikedKeywords.stream().limit(10).collect(Collectors.joining("', '")))
                        .append("'. ");
            }
        }
        String result = preferences.toString().trim();
        if (!result.isBlank()) {
            System.out.println("Aggregated Preferences for LLM: " + result);
        }
        return result;
    }

    // For debugging or advanced use
    public java.util.Map<String, Set<String>> getAllPreferences() {
        return java.util.Map.of("liked", new LinkedHashSet<>(likedKeywords), "disliked",
                new LinkedHashSet<>(dislikedKeywords));
    }
}