package com.example.ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AIParsers {

    private AIParsers() {
    }

    public static Parsed parseCaptionAndTags(String raw) {
        if (raw == null)
            return new Parsed(null, List.of());
        String s = raw.trim();
        int sep = s.indexOf(":::");
        String caption = (sep > 0) ? s.substring(0, sep).trim() : s;
        String rest = (sep > 0) ? s.substring(sep + 3).trim() : "";
        List<String> tags = new ArrayList<>();
        if (!rest.isBlank()) {
            Arrays.stream(rest.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(t -> !t.isBlank())
                    .forEach(tags::add);
        }
        return new Parsed(caption, tags);
    }

    public record Parsed(String caption, List<String> tags) {
    }
}
