package org.example.util;

/**
 * Utility class for text processing and formatting.
 */
public class TextUtils {

    /**
     * Cleans up text by removing excessive linebreaks and normalizing whitespace.
     * Converts markdown-style formatting to clean, readable text suitable for JSON responses.
     * 
     * @param text the raw text to clean
     * @return cleaned text with normalized spacing
     */
    public static String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        return text
            // Replace multiple consecutive newlines with single space
            .replaceAll("\\n\\s*\\n+", " ")
            // Replace single newlines (including those with tabs/spaces) with single space
            .replaceAll("\\n\\s*", " ")
            // Replace markdown-style bullet points
            .replaceAll("\\*\\s+", "- ")
            // Replace multiple consecutive spaces with single space
            .replaceAll("\\s+", " ")
            // Trim leading and trailing whitespace
            .trim();
    }
} 