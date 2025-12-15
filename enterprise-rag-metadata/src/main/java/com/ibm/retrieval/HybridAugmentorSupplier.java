package com.ibm.retrieval;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.microprofile.context.ManagedExecutor;

import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.injector.DefaultContentInjector;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class HybridAugmentorSupplier implements Supplier<RetrievalAugmentor> {

        @Inject
        DatabaseRetriever sqlRetriever;

        @Inject
        DocumentRetriever vectorRetriever;

        @Inject
        ManagedExecutor executor;

        @Override
        public RetrievalAugmentor get() {
                // Create a prompt template that formats retrieved content with metadata
                // The template includes the text content and source information (file name and page number)
                // Note: URL is kept in metadata but not displayed to avoid model hallucinating link formats
                PromptTemplate template = PromptTemplate.from("""
                                
                                
                        Content: {{text}}
                        [Source: {{file_name}} | Page: {{page_number}}]

                        """);

                // Configure the content injector to:
                // - Include specific metadata keys (file_name, page_number, url) in the prompt
                // - Use the custom template to format how content appears in the prompt
                DefaultContentInjector injector = DefaultContentInjector.builder()
                                .metadataKeysToInclude(List.of("file_name", "page_number", "url"))
                                .promptTemplate(template)
                                .build();

                // 1. Query Router: Routes each query to both retrievers (hybrid search)
                // DefaultQueryRouter broadcasts queries to all registered retrievers simultaneously
                // This enables parallel retrieval from both SQL (structured) and vector (semantic) stores
                DefaultQueryRouter router = new DefaultQueryRouter(sqlRetriever, vectorRetriever);

                // 2. Content Aggregator: Combines results from all retrievers
                // LoggingContentAggregator merges results from both SQL and vector retrievers
                // into a single list, with logging for visibility into the aggregation process
                ContentAggregator aggregator = new LoggingContentAggregator();

                // 3. Build the Retrieval Augmentor that orchestrates the hybrid retrieval pipeline:
                // - Router: Sends queries to both retrievers in parallel
                // - Aggregator: Combines results from all retrievers
                // - Injector: Formats the aggregated content with metadata into the prompt
                // - Executor: Manages async execution of retrieval operations
                return DefaultRetrievalAugmentor.builder()
                                .queryRouter(router)
                                .contentAggregator(aggregator)
                                .contentInjector(injector)
                                .executor(executor)
                                .build();
        }

        /**
         * Custom ContentAggregator that combines results from multiple retrievers
         * and provides detailed logging for debugging and monitoring.
         * 
         * Logs include:
         * - Total number of retrievers that contributed results
         * - Total number of queries processed
         * - Total content items retrieved
         * - Per-retriever contribution breakdown
         * - Final aggregated content count
         */
        private static class LoggingContentAggregator implements ContentAggregator {
                @Override
                public List<Content> aggregate(Map<Query, Collection<List<Content>>> queryToContents) {
                        int totalRetrievers = queryToContents.values().stream()
                                        .mapToInt(Collection::size)
                                        .sum();

                        int totalContents = queryToContents.values().stream()
                                        .flatMap(Collection::stream)
                                        .mapToInt(List::size)
                                        .sum();

                        Log.infof(
                                        "HybridAugmentor: Aggregating results from %d retriever(s) across %d query/queries, total %d content item(s)",
                                        totalRetrievers, queryToContents.size(), totalContents);

                        // Log results from each query/retriever combination
                        int retrieverIndex = 0;
                        for (Map.Entry<Query, Collection<List<Content>>> entry : queryToContents.entrySet()) {
                                Query query = entry.getKey();
                                Collection<List<Content>> contentsLists = entry.getValue();
                                for (List<Content> contents : contentsLists) {
                                        retrieverIndex++;
                                        Log.infof("HybridAugmentor: Retriever [%d] for query '%s' contributed %d content item(s)",
                                                        retrieverIndex, query.text(), contents.size());
                                }
                        }

                        // Use default aggregation (combines all results)
                        List<Content> aggregated = queryToContents.values().stream()
                                        .flatMap(Collection::stream)
                                        .flatMap(List::stream)
                                        .toList();

                        Log.infof("HybridAugmentor: Final aggregated result contains %d content item(s) to be injected into prompt",
                                        aggregated.size());

                        return aggregated;
                }
        }
}