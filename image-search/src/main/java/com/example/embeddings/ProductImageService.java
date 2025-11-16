package com.example.embeddings;

import java.util.List;
import java.util.Map;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProductImageService {

    @Inject
    OnnxClipEmbeddingModel clipModel;

    @Inject
    EmbeddingStore<TextSegment> vectorStore;

    @Transactional
    public ProductEntity addProduct(String name, byte[] imageBytes) {

        ProductEntity p = new ProductEntity();
        p.name = name;
        p.imageData = imageBytes;
        p.persist();

        var response = clipModel.embed(imageBytes);
        Embedding embedding = response.content();

        TextSegment segment = TextSegment.from(
                "product:" + p.id,
                Metadata.from(Map.of("name", name)));

        vectorStore.add(embedding, segment);

        return p;
    }

    public List<ProductMatch> findSimilar(byte[] query, int limit) {
        var response = clipModel.embed(query);

        EmbeddingSearchRequest req = EmbeddingSearchRequest.builder()
                .queryEmbedding(response.content())
                .maxResults(limit)
                .minScore(0.75)
                .build();

        var result = vectorStore.search(req);

        return result.matches().stream()
                .map(match -> {
                    long id = Long.parseLong(
                            match.embedded().text().replace("product:", ""));
                    return new ProductMatch(ProductEntity.findById(id), match.score());
                })
                .toList();
    }
}
