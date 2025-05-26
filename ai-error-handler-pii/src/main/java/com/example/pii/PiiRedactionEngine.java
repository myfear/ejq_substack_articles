package com.example.pii;

import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PiiRedactionEngine {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[\\w.-]+@[\\w.-]+\\.[a-z]{2,}\\b");

    public String redact(String text) {
        return EMAIL_PATTERN.matcher(text).replaceAll("[REDACTED_EMAIL]");
    }
}