package org.acme;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadabilityUtil {

    public static double calculateFleschKincaidReadingEase(String text) {
        if (text == null || text.isBlank()) {
            return 0.0;
        }

        long wordCount = countWords(text);
        long sentenceCount = countSentences(text);
        long syllableCount = countSyllables(text);

        if (wordCount == 0 || sentenceCount == 0) {
            return 0.0;
        }

        // Flesch Reading Ease formula
        return 206.835
                - 1.015 * ((double) wordCount / sentenceCount)
                - 84.6 * ((double) syllableCount / wordCount);
    }

    public static long countWords(String text) {
        // Simple word count based on spaces
        return text.trim().split("\\s+").length;
    }

    public static long countSentences(String text) {
        // Count sentences based on terminal punctuation
        Matcher matcher = Pattern.compile("[.?!]").matcher(text);
        long count = 0;
        while (matcher.find()) {
            count++;
        }
        return count > 0 ? count : 1; // Assume at least one sentence
    }

    public static long countSyllables(String text) {
        long count = 0;
        String[] words = text.toLowerCase().replaceAll("[^a-z\\s]", "").split("\\s+");
        for (String word : words) {
            count += getSyllablesInWord(word);
        }
        return count;
    }

    public static int getSyllablesInWord(String word) {
        // Regex-based syllable counting logic adapted from the source
        if (word == null || word.isBlank()) {
            return 0;
        }

        word = word.toLowerCase().trim();
        if (word.length() <= 3) {
            return 1;
        }
        
        // Handle common suffixes
        word = word.replaceAll("e$", "");
        
        String[] split = word.split("[aeiouy]+");
        int syllableCount = 0;
        
        for (String s : split) {
            if (!s.isBlank()) {
                syllableCount++;
            }
        }
        
        // A single vowel at the end often doesn't form a new syllable
        if (word.length() > 1 && (word.endsWith("a") || word.endsWith("i") || word.endsWith("o") || word.endsWith("u"))) {
            // This is a simplification; a more complex model would be needed for perfect accuracy
        }

        return Math.max(syllableCount, 1);
    }
}