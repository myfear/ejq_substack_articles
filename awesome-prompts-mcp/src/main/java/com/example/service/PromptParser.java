package com.example.service;

import com.example.model.Prompt;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
public class PromptParser {

    // Markdown + YAML patterns
    private static final Pattern TITLE = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern CATEGORY = Pattern.compile("(?i)category[:\\s]+([\\w\\s-]+)");
    private static final Pattern TAGS = Pattern.compile("(?i)tags[:\\s]+(.+)$", Pattern.MULTILINE);
    private static final Pattern FRONT_MATTER = Pattern.compile("^---\\s*\\R(?s)(.*?)\\R---\\s*", Pattern.MULTILINE);
    private static final Pattern FM_LINE = Pattern.compile("^([A-Za-z0-9_-]+)\\s*:\\s*(.+?)\\s*$", Pattern.MULTILINE);

    public Prompt parseMarkdownFile(String content, String path) {
        Map<String, String> fm = parseFrontMatter(content);

        String id = fm.getOrDefault("id", generateIdFromPath(path));
        String title = extract(TITLE, content, fm.getOrDefault("title", "Untitled Prompt"));
        String description = fm.getOrDefault("description", extractDescription(content));

        Prompt.PromptCategory category = parseCategory(
                fm.getOrDefault("category", extract(CATEGORY, content, null)),
                path,
                content);

        List<String> tags = parseTags(fm);
        if (tags.isEmpty())
            tags = extractTagsFromBody(content);

        return new Prompt(
                id,
                title,
                description,
                content,
                category,
                tags,
                path,
                "");
    }

    /* ----------------- YAML front-matter parsing ----------------- */

    private Map<String, String> parseFrontMatter(String content) {
        Matcher m = FRONT_MATTER.matcher(content);
        if (!m.find())
            return Collections.emptyMap();

        String block = m.group(1);
        Map<String, String> map = new LinkedHashMap<>();
        Matcher line = FM_LINE.matcher(block);
        while (line.find()) {
            String key = line.group(1).trim().toLowerCase(Locale.ROOT);
            String val = stripQuotes(line.group(2).trim());
            map.put(key, val);
        }
        return map;
    }

    private String stripQuotes(String s) {
        if ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\""))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    /* ----------------- Tag handling ----------------- */

    private List<String> parseTags(Map<String, String> fm) {
        String raw = fm.getOrDefault("tags", fm.get("mode")); // fall back to "mode"
        if (raw == null || raw.isBlank())
            return List.of();

        // Accept comma, semicolon, or space separation
        String normalized = raw.replaceAll("[;]", ",");
        String[] parts = normalized.contains(",") ? normalized.split(",") : normalized.split("\\s+");

        Set<String> result = new LinkedHashSet<>();
        for (String part : parts) {
            String t = part.trim().toLowerCase(Locale.ROOT);
            if (!t.isEmpty())
                result.add(t);
        }
        return new ArrayList<>(result);
    }

    private List<String> extractTagsFromBody(String content) {
        String raw = extract(TAGS, content, null);
        if (raw == null || raw.isBlank())
            return List.of();
        String[] parts = raw.replaceAll("[;]", ",").split("[,\\s]+");
        List<String> tags = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim().toLowerCase(Locale.ROOT);
            if (!t.isEmpty())
                tags.add(t);
        }
        return tags;
    }

    /* ----------------- Category inference ----------------- */

    private Prompt.PromptCategory parseCategory(String rawCategory, String path, String content) {
        if (rawCategory != null && !rawCategory.isBlank()) {
            try {
                return Prompt.PromptCategory.valueOf(
                        rawCategory.toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "_"));
            } catch (IllegalArgumentException ignored) {
                /* fall through */ }
        }
        return inferCategory(path, content);
    }

    private Prompt.PromptCategory inferCategory(String path, String content) {
        String all = (path + " " + content).toLowerCase(Locale.ROOT);

        if (all.contains("architecture") || all.contains("arch"))
            return Prompt.PromptCategory.ARCHITECTURE;
        if (all.contains("review"))
            return Prompt.PromptCategory.CODE_REVIEW;
        if (all.contains("test"))
            return Prompt.PromptCategory.TESTING;
        if (all.contains("doc"))
            return Prompt.PromptCategory.DOCUMENTATION;
        if (all.contains("refactor"))
            return Prompt.PromptCategory.REFACTORING;
        if (all.contains("debug"))
            return Prompt.PromptCategory.DEBUGGING;
        if (all.contains("security"))
            return Prompt.PromptCategory.SECURITY;
        if (all.contains("performance") || all.contains("perf"))
            return Prompt.PromptCategory.PERFORMANCE;

        return Prompt.PromptCategory.GENERAL;
    }

    /* ----------------- Description and title helpers ----------------- */

    private String extract(Pattern p, String text, String def) {
        if (text == null)
            return def;
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : def;
    }

    private String extractDescription(String text) {
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        boolean afterTitle = false;
        for (String line : lines) {
            if (line.startsWith("#")) {
                afterTitle = true;
                continue;
            }
            if (afterTitle && !line.isBlank())
                sb.append(line.trim()).append(' ');
            if (sb.length() > 200)
                break;
        }
        return sb.toString().trim();
    }

    /* ----------------- ID generation ----------------- */

    private String generateIdFromPath(String path) {
        String filename = Path.of(path).getFileName().toString();
        filename = filename.replaceFirst("(?i)\\.prompt\\.md$", "").replaceFirst("(?i)\\.md$", "");
        String base = filename.replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("^-+|-+$", "")
                .toLowerCase(Locale.ROOT);
        // Stable 6-char suffix from full relative path
        String shortHash = Integer.toHexString(path.toLowerCase(Locale.ROOT).hashCode());
        shortHash = shortHash.length() > 6 ? shortHash.substring(0, 6) : shortHash;
        return base + "-" + shortHash;
    }

}
