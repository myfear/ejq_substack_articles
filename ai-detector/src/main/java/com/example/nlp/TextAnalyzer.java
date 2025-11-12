package com.example.nlp;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import jakarta.enterprise.context.ApplicationScoped;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.SimpleTokenizer;

@ApplicationScoped
public class TextAnalyzer {

    private final SentenceDetectorME sentenceDetector;
    private final SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;

    public TextAnalyzer() {
        try (InputStream modelIn = getClass().getResourceAsStream("/models/en-sent.bin")) {
            sentenceDetector = new SentenceDetectorME(new SentenceModel(modelIn));
        } catch (Exception e) {
            throw new IllegalStateException("Sentence model missing", e);
        }
    }

    public Map<String, Object> computeStats(String text) {
        Map<String, Object> stats = new LinkedHashMap<>();

        String[] sentences = sentenceDetector.sentDetect(text);
        int sentenceCount = sentences.length;

        List<Integer> lengths = new ArrayList<>();
        List<String> allTokens = new ArrayList<>();

        for (String s : sentences) {
            String[] tokens = tokenizer.tokenize(s);
            allTokens.addAll(Arrays.asList(tokens));
            lengths.add(tokens.length);
        }

        DescriptiveStatistics ds = new DescriptiveStatistics();
        lengths.forEach(ds::addValue);

        double meanLen = ds.getMean();
        double burstiness = ds.getStandardDeviation();
        double lexicalDiversity = new HashSet<>(allTokens).size() / (double) allTokens.size();

        stats.put("sentences", sentenceCount);
        stats.put("mean_sentence_length", meanLen);
        stats.put("burstiness", burstiness);
        stats.put("lexical_diversity", lexicalDiversity);

        // Word frequency for common AI markers and phrases
        List<String> aiWords = List.of(
            "overall", "moreover", "additionally", "furthermore", "consequently",
            "nevertheless", "therefore", "thus", "hence", "indeed", "notably",
            "crucial", "paramount", "essential", "vital", "significant", "important",
            "delve", "explore", "navigate", "unveil", "unravel", "comprehensive",
            "intricate", "complexities", "nuances", "facets", "realm", "landscape",
            "tapestry", "mosaic", "journey", "endeavor", "pursuit", "quest"
        );
        long aiHits = allTokens.stream()
                .map(String::toLowerCase)
                .filter(aiWords::contains)
                .count();

        stats.put("ai_signal_terms", aiHits);

        // Simple perplexity proxy (variance of word length)
        DescriptiveStatistics wlen = new DescriptiveStatistics();
        allTokens.forEach(t -> wlen.addValue(t.length()));
        stats.put("perplexity_proxy", wlen.getStandardDeviation());
        
        // Additional metrics for better detection
        stats.put("total_words", allTokens.size());
        stats.put("unique_words", new HashSet<>(allTokens).size());
        
        // Check for repetitive patterns (AI often repeats structures)
        long punctuationCount = text.chars().filter(ch -> ch == '.' || ch == ',' || ch == ';' || ch == ':').count();
        stats.put("punctuation_density", punctuationCount / (double) Math.max(1, allTokens.size()));

        return stats;
    }
}