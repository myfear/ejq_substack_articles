package com.example.guardrails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BMPromptInjectionGuardrail implements InputGuardrail {

    // Common prompt injection patterns (case-insensitive)
    private static final String[] INJECTION_PATTERNS = {
            // Direct instruction overrides
            "ignore previous instructions",
            "forget everything above",
            "disregard the above",
            "new instructions:",
            "system:",
            "assistant:",
            "override mode",

            // Role manipulation
            "you are now",
            "act as",
            "pretend to be",
            "roleplay as",
            "simulate being",

            // Jailbreak attempts
            "jailbreak",
            "developer mode",
            "unrestricted mode",
            "evil mode",
            "dan mode",

            // Information extraction
            "reveal your instructions",
            "show me your prompt",
            "what are your guidelines",
            "tell me your rules",

            // Encoding tricks
            "base64:",
            "rot13:",
            "reverse:",

            // Common delimiters used in injections
            "---",
            "###",
            "```",
            "===",
            "***"
    };

    private List<BoyerMoorePattern> patterns;

    @PostConstruct
    public void init() {
        Log.infof("Initializing Boyer-Moore Prompt Injection Guardrail with {} patterns",
                INJECTION_PATTERNS.length);

        this.patterns = new ArrayList<>();
        for (String pattern : INJECTION_PATTERNS) {
            patterns.add(new BoyerMoorePattern(pattern.toLowerCase()));
        }
    }

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        Log.debug("Boyer-Moore PromptInjectionGuardrail validating message");

        String text = userMessage.singleText();
        if (text == null || text.isEmpty()) {
            return success();
        }

        String lowerText = text.toLowerCase();

        // Scan for injection patterns using Boyer-Moore
        for (BoyerMoorePattern pattern : patterns) {
            int position = pattern.search(lowerText);
            if (position != -1) {
                String context = extractContext(text, position, pattern.getPattern().length());
                String failureMessage = String.format(
                        "Prompt injection detected: pattern '%s' found at position %d. Context: '...%s...'",
                        pattern.getPattern(), position, context);

                Log.warnf("Prompt injection blocked: {}", failureMessage);
                return failure(failureMessage);
            }
        }

        Log.debug("Input validated successfully - no prompt injection detected");
        return success();
    }

    private String extractContext(String input, int position, int patternLength) {
        int start = Math.max(0, position - 15);
        int end = Math.min(input.length(), position + patternLength + 15);
        return input.substring(start, end);
    }

    /**
     * Boyer-Moore string matching implementation optimized for multiple pattern
     * scanning
     */
    private static class BoyerMoorePattern {
        private final String pattern;
        private final int[] badCharTable;
        private final int patternLength;

        public BoyerMoorePattern(String pattern) {
            this.pattern = pattern;
            this.patternLength = pattern.length();
            this.badCharTable = buildBadCharTable(pattern);
        }

        /**
         * Build bad character table for Boyer-Moore algorithm
         */
        private int[] buildBadCharTable(String pattern) {
            int[] table = new int[256]; // ASCII character set
            Arrays.fill(table, patternLength);

            // Fill table with skip distances for each character
            for (int i = 0; i < patternLength - 1; i++) {
                char c = pattern.charAt(i);
                table[c & 0xFF] = patternLength - 1 - i;
            }

            return table;
        }

        /**
         * Search for pattern in text using Boyer-Moore algorithm
         * 
         * @param text Text to search in (should be lowercase)
         * @return Position of first match, or -1 if not found
         */
        public int search(String text) {
            if (text.length() < patternLength) {
                return -1;
            }

            int textIndex = patternLength - 1;

            while (textIndex < text.length()) {
                int patternIndex = patternLength - 1;
                int currentTextIndex = textIndex;

                // Match pattern from right to left
                while (patternIndex >= 0 &&
                        pattern.charAt(patternIndex) == text.charAt(currentTextIndex)) {
                    patternIndex--;
                    currentTextIndex--;
                }

                if (patternIndex < 0) {
                    return currentTextIndex + 1; // Match found
                }

                // Use bad character heuristic to skip characters
                char badChar = text.charAt(textIndex);
                int skip = badCharTable[badChar & 0xFF];
                textIndex += Math.max(1, skip); // Ensure we always advance at least 1
            }

            return -1; // No match found
        }

        public String getPattern() {
            return pattern;
        }
    }
}