package com.example.wordcloud.app;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import com.example.wordcloud.api.CloudParams;
import com.example.wordcloud.core.WordCloudRenderer;
import com.example.wordcloud.core.WordCloudRenderer.CloudConfig;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WordCloudService {

    public BufferedImage render(String text, CloudParams p) {
        List<String> tokens = tokenize(text);
        CloudConfig cfg = toCfg(p);
        return WordCloudRenderer.renderPng(tokens, cfg);
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank())
            return List.of("quarkus", "java", "word", "cloud");
        String norm = text.toLowerCase().replaceAll("[^\\p{L}\\p{Nd}\\s]+", " ");
        return Arrays.asList(norm.trim().split("\\s+"));
    }

    private CloudConfig toCfg(CloudParams p) {
        CloudConfig c = new CloudConfig();
        c.width = p.width;
        c.height = p.height;
        c.maxWords = p.maxWords;
        c.minFont = p.minFont;
        c.maxFont = p.maxFont;
        c.rotateSome = p.rotateSome;
        c.rotateProb = p.rotateProb;
        c.localRewordle = p.localRewordle;
        c.fontFamily = p.fontFamily;
        c.seed = p.seed;
        return c;
    }
}