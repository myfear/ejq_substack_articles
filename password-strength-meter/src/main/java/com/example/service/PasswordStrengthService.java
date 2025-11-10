package com.example.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.analysis.PhoneticMatcher;
import com.example.analysis.SegmentAnalyzer;
import com.example.analysis.SyllableAnalyzer;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordStrengthService {

    private final PhoneticMatcher matcher = new PhoneticMatcher();

    public Map<String, Object> evaluate(String password) {
        // Track if password was originally null to return null in response
        boolean wasNull = (password == null);
        
        // Handle null password - treat as empty string for evaluation
        String passwordForEvaluation = (password == null) ? "" : password;
        
        int score = 100;
        List<String> warnings = new ArrayList<>();
        boolean hasPhoneticMatch = false;

        // Traditional checks
        if (passwordForEvaluation.length() < 8) {
            score -= 20;
            warnings.add("Too short (min 8 chars)");
        }

        // Syllable density
        double density = SyllableAnalyzer.getSyllableDensity(passwordForEvaluation);
        if (density > 0.5) {
            score -= 20;
            warnings.add("Too easy to pronounce");
        } else if (density < 0.2) {
            score += 5;
        }

        // Phonetic similarity 
        var result = matcher.check(passwordForEvaluation);
        if (result.match()) {
            score -= 40; 
            hasPhoneticMatch = true;
            warnings.add("Sounds like common word: " + result.pattern());
        }

        // Segment pronounceability
        double pron = SegmentAnalyzer.pronounceability(passwordForEvaluation);
        if (pron > 0.7) {
            score -= 15;
            warnings.add("Highly pronounceable segments");
        }

        // Ensure score is between 0 and 100
        score = Math.max(0, Math.min(100, score));

        // Return null for password field if it was originally null
        // Use HashMap to allow null values (Map.of doesn't allow null)
        Map<String, Object> response = new HashMap<>();
        response.put("password", wasNull ? null : password);
        response.put("score", score);
        response.put("warnings", warnings);
        response.put("rating", rating(score, hasPhoneticMatch));
        return response;
    }

    private String rating(int score, boolean hasPhoneticMatch) {
        // Phonetic similarity to common words is a serious security issue
        // Cap the rating at "GOOD" even if score would be higher
        if (hasPhoneticMatch && score >= 60) {
            return "GOOD";
        }
        if (hasPhoneticMatch && score >= 40) {
            return "FAIR";
        }
        
        if (score >= 80)
            return "STRONG";
        if (score >= 60)
            return "GOOD";
        if (score >= 40)
            return "FAIR";
        if (score >= 20)
            return "WEAK";
        return "VERY_WEAK";
    }
}