package com.example.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.CosineSimilarity;
import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;

@ApplicationScoped
public class EmbeddingService {

    @Inject
    EmbeddingModel model;

    private final Map<String, Embedding> reference = new HashMap<>();

    public void initReference() {
        reference.put("human", model.embed("A deeply personal reflection on growing up by the ocean.").content());
        reference.put("ai", model.embed("This article highlights the importance of artificial intelligence in modern society.").content());
    }

    public double compare(String text) {
        if (reference.isEmpty()) initReference();
        Embedding input = model.embed(text).content();
        double simHuman = CosineSimilarity.between(input, reference.get("human"));
        double simAI = CosineSimilarity.between(input, reference.get("ai"));
        return simAI - simHuman; // >0 = closer to AI
    }
}