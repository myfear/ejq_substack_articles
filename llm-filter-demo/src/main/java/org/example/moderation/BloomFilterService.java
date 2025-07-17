package org.example.moderation;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BloomFilterService {

    private static final List<String> PROHIBITED_PHRASES = List.of(
            "buy illegal items",
            "secret cheat codes",
            "prohibited substance",
            "malicious download link");

    private static final int N_GRAM_SIZE = 3;
    private BloomFilter<CharSequence> filter;

    @PostConstruct
    void initialize() {
        filter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                1000,
                0.01);

        PROHIBITED_PHRASES.forEach(phrase -> filter.put(phrase.toLowerCase()));
    }

    public boolean mightContainProblematicNgram(String content) {
        if (content == null || content.isBlank())
            return false;

        String[] words = content.toLowerCase().trim().split("\\s+");

        if (words.length < N_GRAM_SIZE) {
            return filter.mightContain(String.join(" ", words));
        }

        for (int i = 0; i <= words.length - N_GRAM_SIZE; i++) {
            StringBuilder ngramBuilder = new StringBuilder();
            for (int j = 0; j < N_GRAM_SIZE; j++) {
                ngramBuilder.append(words[i + j]).append(" ");
            }
            String ngram = ngramBuilder.toString().trim();

            if (filter.mightContain(ngram)) {
                return true;
            }
        }

        return false;
    }
}