package com.example.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.example.ai.AIParsers;
import com.example.ai.MemeAI;
import com.example.model.Meme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MemeService {

    @Inject
    MemeStore store;
    @ConfigProperty(name = "meme.ai.enabled", defaultValue = "true")
    boolean aiEnabled;
    @Inject
    MemeAI memeAI;

    public Meme create(Meme input) {
        Meme normalized = normalize(input);
        if (aiEnabled) {
            var raw = memeAI.captionAndTags(new MemeAI.MemePrompt(
                    normalized.getTitle(),
                    normalized.getTags()));
            var parsed = AIParsers.parseCaptionAndTags(raw);
            normalized.setAiCaption(parsed.caption());
            normalized.setAiTags(parsed.tags());
        }
        return store.save(normalized);
    }

    private Meme normalize(Meme in) {
        // Already handled by setters/Jackson; hook for future logic
        return in;
    }
}