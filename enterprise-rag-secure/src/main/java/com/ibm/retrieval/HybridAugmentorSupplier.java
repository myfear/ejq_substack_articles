package com.ibm.retrieval;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.microprofile.context.ManagedExecutor;

import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
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

                // 1. The Router: Decides where to go.
                // In this simple case, we use the DefaultQueryRouter which
                // essentially broadcasts to all registered retrievers.
                // If you wanted conditional logic (e.g. "Only check SQL if text contains
                // 'ORD-'"),
                // you would implement a custom QueryRouter here.
                DefaultQueryRouter router = new DefaultQueryRouter(sqlRetriever, vectorRetriever);

                // 2. The Aggregator: Merges the results.
                // It runs everything, collects segments, and puts them into the prompt.
                ContentAggregator aggregator = new LoggingContentAggregator();

                // 3. Build the Augmentor
                return DefaultRetrievalAugmentor.builder()
                                .queryRouter(router)
                                .contentAggregator(aggregator)
                                .executor(executor)
                                .build();
        }

        /**
         * Custom ContentAggregator that logs the final aggregated results
         * to verify that both retrievers' results are being combined.
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