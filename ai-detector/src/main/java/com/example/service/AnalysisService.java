package com.example.service;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.nlp.TextAnalyzer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AnalysisService {

    @Inject
    TextAnalyzer analyzer;

    @Inject
    EmbeddingService embedding;

    public Map<String, Object> analyze(String text) {
        if (text == null || text.isBlank()) {
            return Map.of("error", "No text provided");
        }

        Map<String, Object> stats = analyzer.computeStats(text);

        int sentenceCount = (int) stats.get("sentences");
        double meanSentenceLength = (double) stats.get("mean_sentence_length");
        double burstiness = (double) stats.get("burstiness");
        double diversity = (double) stats.get("lexical_diversity");
        long aiWords = (long) stats.get("ai_signal_terms");
        double perplexityProxy = (double) stats.get("perplexity_proxy");
        int totalWords = (int) stats.get("total_words");
        double punctuationDensity = (double) stats.get("punctuation_density");

        // Improved scoring heuristic
        double aiScore = 0.0;

        // 1. Burstiness: AI text tends to have low variation in sentence length
        // For single sentence, use mean length as indicator instead
        if (sentenceCount == 1) {
            // Single long sentences are often AI-generated
            if (meanSentenceLength > 15) {
                aiScore += 0.25;
            }
        } else {
            // Multiple sentences: low burstiness indicates AI
            if (burstiness < 3.0) {
                aiScore += 0.2;
            }
        }

        // 2. Lexical diversity: AI often uses more repetitive vocabulary
        // But for very short texts, this metric is unreliable
        if (totalWords > 20) {
            if (diversity < 0.4) {
                aiScore += 0.2;
            }
        } else {
            // For short texts, very high diversity (all unique words) can be suspicious
            if (diversity > 0.9 && totalWords > 10) {
                aiScore += 0.15;
            }
        }

        // 3. AI signal terms: common phrases in AI-generated text
        double aiWordRatio = aiWords / (double) Math.max(1, totalWords);
        if (aiWords >= 1) {
            aiScore += Math.min(0.3, aiWordRatio * 10);
        }

        // 4. Mean sentence length: AI often generates longer, more complex sentences
        if (meanSentenceLength > 18) {
            aiScore += 0.15;
        }

        // 5. Perplexity proxy: low variance in word length suggests AI
        if (perplexityProxy < 2.5 && totalWords > 15) {
            aiScore += 0.1;
        }

        // 6. Punctuation density: AI often uses more formal punctuation
        if (punctuationDensity > 0.15) {
            aiScore += 0.1;
        }

        // 7. Very short texts with high complexity are suspicious
        if (totalWords < 30 && meanSentenceLength > 12) {
            aiScore += 0.15;
        }

        // 8. Semantic embedding analysis: compare text similarity to AI vs human reference texts
        double semanticBias = embedding.compare(text);
        if (semanticBias > 0) {
            // Text is semantically closer to AI reference
            aiScore += 0.2;
        } else {
            // Text is semantically closer to human reference
            aiScore -= 0.1;
        }

        // Normalize score to 0-1 range
        aiScore = Math.max(0.0, Math.min(1.0, aiScore));

        String label = aiScore > 0.5 ? "AI-like" : "Human-like";

        Map<String, Object> result = new LinkedHashMap<>(stats);
        result.put("semantic_bias", semanticBias);
        result.put("ai_score", aiScore);
        result.put("classification", label);
        return result;
    }
}