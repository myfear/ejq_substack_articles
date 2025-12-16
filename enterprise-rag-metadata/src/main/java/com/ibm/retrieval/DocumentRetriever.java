package com.ibm.retrieval;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocumentRetriever implements ContentRetriever {

    private final EmbeddingStoreContentRetriever contentRetriever;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    DocumentRetriever(EmbeddingStore<TextSegment> store, EmbeddingModel model) {
        this.embeddingStore = store;
        this.embeddingModel = model;
        this.contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(model)
                .embeddingStore(store)
                .maxResults(10)
                .minScore(0.7)
                .build();
    }

    @Override
    public List<Content> retrieve(Query query) {
        Log.infof("DocumentRetriever: Processing query: %s", query.text());

        Embedding queryEmbedding = embeddingModel.embed(query.text()).content();

        EmbeddingSearchResult<TextSegment> results = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(3)
                        .minScore(0.7)
                        .build());

        // Perform the retrieval using the search results
        List<Content> contents = results.matches().stream()
                .map(match -> {
                    TextSegment segment = match.embedded();

                    // Enrich with vector-specific metadata
                    Metadata enriched = segment.metadata().copy();

                    // Vector-specific enrichment
                    enriched.put("retrieval_method", "vector_search");
                    enriched.put("similarity_score", match.score());
                    enriched.put("embedding_id", match.embeddingId());
                    enriched.put("retrieval_timestamp", Instant.now().toString());

                    // Create new TextSegment with enriched metadata
                    TextSegment enrichedSegment = TextSegment.from(segment.text(), enriched);
                    return Content.from(enrichedSegment);
                })
                .collect(Collectors.toList());

        // Log retrieved content snippets for developer visibility
        // This helps developers understand how many documents are being retrieved
        Log.infof("DocumentRetriever: Retrieved %d document snippet(s) for augmentation", contents.size());

        return contents;
    }

}