package org.acme.aiservices;

import java.util.List;
import java.util.stream.Collectors;

import org.acme.services.MemoryRetrievalService;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class MemoryContentRetriever implements ContentRetriever {

    @Inject
    MemoryRetrievalService retrievalService;

    @Override
    public List<Content> retrieve(Query query) {
        Log.infof("ContentRetriever: %s", query);
        // Use our custom, multi-factor retrieval service
        List<EmbeddingMatch<TextSegment>> matches = retrievalService.retrieveAndRankMemories(query.text(), 5);

        // Convert the results into the format Langchain4j expects
        return matches.stream()
                .map(EmbeddingMatch::embedded)
                .map(Content::from)
                .collect(Collectors.toList());
    }
}
