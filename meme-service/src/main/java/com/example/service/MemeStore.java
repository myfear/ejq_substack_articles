package com.example.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.example.model.Meme;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MemeStore {

    private final Map<UUID, Meme> data = new ConcurrentHashMap<>();

    public Meme save(Meme m) {
        if (m.getId() == null)
            m.setId(UUID.randomUUID());
        if (m.getCreatedAt() == null)
            m.setCreatedAt(Instant.now());
        data.put(m.getId(), m);
        return m;
    }

    public Optional<Meme> find(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    public List<Meme> list(Optional<String> tag) {
        return data.values().stream()
                .filter(m -> tag.map(t -> hasTag(m, t)).orElse(true))
                .sorted(Comparator.comparing(Meme::getCreatedAt).reversed())
                .toList();
    }

    public Optional<Meme> random(Optional<String> tag) {
        List<Meme> list = list(tag);
        if (list.isEmpty())
            return Optional.empty();
        return Optional.of(list.get(new Random().nextInt(list.size())));
    }

    private boolean hasTag(Meme m, String t) {
        if (m.getTags() == null)
            return false;
        String needle = t.toLowerCase();
        return m.getTags().stream().anyMatch(x -> x.equalsIgnoreCase(needle));
    }
}