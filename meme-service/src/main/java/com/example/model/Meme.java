package com.example.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.json.LenientInstantDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Meme {

    @JsonProperty("id")
    private UUID id;

    @NotBlank
    @Size(max = 120)
    private String title;

    @JsonAlias({ "img", "image", "image_url" })
    @NotBlank
    @Size(max = 1024)
    private String imageUrl;

    @Size(max = 64)
    private String author;

    @Min(0)
    @Max(5)
    private Integer rating; // 0..5

    // Comma-separated input accepted, normalized to lower-cased list
    private List<String> tags;

    private Boolean nsfw;

    @JsonDeserialize(using = LenientInstantDeserializer.class)
    private Instant createdAt;

    // Enriched by AI
    private String aiCaption;
    private List<String> aiTags;

    public Meme() {
    }

    public Meme(UUID id, String title, String imageUrl, String author, Integer rating,
            List<String> tags, Boolean nsfw, Instant createdAt, String aiCaption, List<String> aiTags) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.author = author;
        this.rating = rating;
        this.tags = tags;
        this.nsfw = nsfw;
        this.createdAt = createdAt;
        this.aiCaption = aiCaption;
        this.aiTags = aiTags;
    }

    // Getters + setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl.trim() : null;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author != null ? author.trim() : null;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(Object value) {
        // Accept ["cat","funny"] or "cat, funny"
        if (value == null) {
            this.tags = null;
            return;
        }
        if (value instanceof List<?> l) {
            this.tags = normalizeStrings(l);
            return;
        }
        String s = value.toString();
        if (s.isBlank()) {
            this.tags = List.of();
            return;
        }
        String[] parts = s.split(",");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim().toLowerCase();
            if (!t.isEmpty())
                out.add(t);
        }
        this.tags = out;
    }

    public Boolean getNsfw() {
        return nsfw;
    }

    public void setNsfw(Boolean nsfw) {
        this.nsfw = nsfw;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getAiCaption() {
        return aiCaption;
    }

    public void setAiCaption(String aiCaption) {
        this.aiCaption = aiCaption;
    }

    public List<String> getAiTags() {
        return aiTags;
    }

    public void setAiTags(List<String> aiTags) {
        this.aiTags = aiTags;
    }

    private static List<String> normalizeStrings(List<?> raw) {
        List<String> out = new ArrayList<>();
        for (Object o : raw) {
            if (o == null)
                continue;
            String s = o.toString().trim().toLowerCase();
            if (!s.isEmpty())
                out.add(s);
        }
        return out;
    }
}