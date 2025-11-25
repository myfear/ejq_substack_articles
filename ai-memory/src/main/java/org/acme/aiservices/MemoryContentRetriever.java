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

/**
 * A content retriever implementation for Langchain4j RAG (Retrieval-Augmented Generation)
 * that retrieves relevant memory fragments from the system's memory store.
 * 
 * <p>This class bridges the gap between Langchain4j's RAG framework and the custom
 * memory retrieval system. It uses a multi-factor retrieval service to find and rank
 * the most relevant memories based on the user's query, then converts them into the
 * format expected by Langchain4j's content augmentation pipeline.
 * 
 * <p>The retriever is configured as a singleton to ensure efficient resource usage
 * and consistent behavior across the application.
 * 
 * @see ContentRetriever
 * @see MemoryRetrievalService
 */
@Singleton
public class MemoryContentRetriever implements ContentRetriever {

    /**
     * The service responsible for retrieving and ranking memory fragments
     * based on semantic similarity and other factors.
     */
    @Inject
    MemoryRetrievalService retrievalService;

    /**
     * Retrieves relevant content from the memory store based on the provided query.
     * 
     * <p>This method uses the custom multi-factor retrieval service to find the top
     * 5 most relevant memory fragments. The results are then converted from
     * {@link EmbeddingMatch} objects to {@link Content} objects that Langchain4j
     * can use for content augmentation.
     * 
     * @param query the query object containing the user's question or request
     * @return a list of {@link Content} objects representing the most relevant
     *         memory fragments, ordered by relevance (most relevant first)
     */
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
