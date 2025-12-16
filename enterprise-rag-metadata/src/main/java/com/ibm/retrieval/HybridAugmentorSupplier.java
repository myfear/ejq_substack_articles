package com.ibm.retrieval;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.microprofile.context.ManagedExecutor;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
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
                // Use a custom content injector that formats content more explicitly
                // This ensures the LLM sees the context clearly formatted and receives
                // explicit instructions to return plain text (not JSON)
                ContentInjector injector = new CustomContentInjector();

                // 1. Query Router: Routes each query to both retrievers (hybrid search)
                // DefaultQueryRouter broadcasts queries to all registered retrievers
                // simultaneously
                // This enables parallel retrieval from both SQL (structured) and vector
                // (semantic) stores
                DefaultQueryRouter router = new DefaultQueryRouter(sqlRetriever, vectorRetriever);

                // 2. Content Aggregator: Combines results from all retrievers
                // LoggingContentAggregator merges results from both SQL and vector retrievers
                // into a single list, with logging for visibility into the aggregation process
                ContentAggregator aggregator = new EnrichedContentAggregator();

                // 3. Build the Retrieval Augmentor that orchestrates the hybrid retrieval
                // pipeline:
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
        private static class EnrichedContentAggregator implements ContentAggregator {
                @Override
                public List<Content> aggregate(Map<Query, Collection<List<Content>>> retrieverToContents) {
                        // Track which doc_ids appear in multiple sources
                        Map<String, List<Content>> contentsByDocId = new HashMap<>();

                        // Collect all content
                        List<Content> allContent = retrieverToContents.values().stream()
                                        .flatMap(Collection::stream)
                                        .flatMap(List::stream)
                                        .peek(content -> {
                                                String docId = content.textSegment().metadata().getString("doc_id");
                                                contentsByDocId.computeIfAbsent(docId, k -> new ArrayList<>())
                                                                .add(content);
                                        })
                                        .collect(Collectors.toList());

                        // Enrich with cross-source metadata
                        return allContent.stream()
                                        .map(content -> enrichWithAggregationMetadata(content, contentsByDocId))
                                        .sorted(Comparator.comparing(EnrichedContentAggregator::calculatePriority)
                                                        .reversed())
                                        .collect(Collectors.toList());
                }

                // The enrichWithAggregationMetadata() method enhances content items with
                // aggregation-level metadata that tracks cross-source validation:
                // Tracks Source Count: Adds found_in_sources_count showing how many retrievers
                // returned this document

                // Cross-Validation Detection: 
                // When a document appears in multiple sources (SQL + vector):

                // Sets cross_validated = "true"
                // Applies confidence_boost = "1.2" (20% boost)
                // Records all retrieval_methods used
                // Logs the cross-validation event
                // Single-Source Handling: For documents from one source only:

                // Sets cross_validated = "false"
                // Applies confidence_boost = "1.0" (no boost)
                // Timestamps: Adds aggregated_at timestamp for tracking

                private static Content enrichWithAggregationMetadata(
                                Content content,
                                Map<String, List<Content>> contentsByDocId) {
                        TextSegment segment = content.textSegment();
                        Metadata enriched = segment.metadata().copy();

                        String docId = enriched.getString("doc_id");
                        List<Content> duplicates = contentsByDocId.get(docId);

                        // Add aggregation-level metadata
                        enriched.put("found_in_sources_count", String.valueOf(duplicates.size()));

                        if (duplicates.size() > 1) {
                                // Document found in multiple retrievers - higher confidence
                                enriched.put("cross_validated", "true");
                                enriched.put("confidence_boost", "1.2");

                                // Collect retrieval methods
                                String methods = duplicates.stream()
                                                .map(c -> c.textSegment().metadata().getString("retrieval_method"))
                                                .distinct()
                                                .collect(Collectors.joining(", "));
                                enriched.put("retrieval_methods", methods);

                                Log.infof("Document %s found in multiple sources: %s", docId, methods);
                        } else {
                                enriched.put("cross_validated", "false");
                                enriched.put("confidence_boost", "1.0");
                        }

                        // Add aggregation timestamp
                        enriched.put("aggregated_at", Instant.now().toString());

                        return Content.from(TextSegment.from(segment.text(), enriched));
                }

                // The calculatePriority() method in the EnrichedContentAggregator class
                // calculates a priority score for each retrieved content item to determine its
                // ranking in the final result set.

                private static double calculatePriority(Content content) {
                        Metadata meta = content.textSegment().metadata();

                        // Base score from retrieval
                        double score = meta.getDouble("similarity_score");

                        // Apply confidence boost for cross-validated content
                        double boost = Double.parseDouble(meta.getString("confidence_boost"));

                        return score * boost;
                }

        }

        /**
         * Custom ContentInjector that formats retrieved content with metadata in a
         * clear,
         * explicit format that helps the LLM understand the context better.
         */
        private static class CustomContentInjector implements ContentInjector {
                @Override
                public ChatMessage inject(List<Content> contents, ChatMessage userMessage) {
                        if (contents == null || contents.isEmpty()) {
                                Log.warn("CustomContentInjector: No content to inject");
                                return userMessage;
                        }

                        // Which metadata keys to include in the prompt
                        final List<String> metadataKeysToInclude = List.of(
                                        "source_url",
                                        "file_name",
                                        "page_number",
                                        "retrieval_method",
                                        "similarity_score",
                                        "cross_validated",
                                        "retrieval_timestamp");

                        // Build a formatted string with all content items
                        StringBuilder contentBuilder = new StringBuilder();
                        contentBuilder.append("=== RETRIEVED DOCUMENT CONTENT ===\n\n");

                        for (int i = 0; i < contents.size(); i++) {

                                Content content = contents.get(i);
                                contentBuilder.append(String.format("--- Content Item %d ---\n", i + 1));

                                // Extract text from content
                                TextSegment segment = content.textSegment();
                                String text = segment.text();

                                // Extract MetaData
                                Metadata meta = segment.metadata();

                                // Add selected metadata
                                for (String key : metadataKeysToInclude) {
                                        if (meta.containsKey(key)) {
                                                Object value = meta.toMap().get(key);
                                                contentBuilder.append(key).append(": ").append(value).append("\n");
                                        }
                                }

                                // Add the actual text content
                                contentBuilder.append("\nContent:\n");
                                if (text != null && !text.isEmpty()) {
                                        contentBuilder.append(text);
                                } else {
                                        contentBuilder.append("(No text content available)");
                                }
                                contentBuilder.append("\n\n");
                        }

                        contentBuilder.append("=== END RETRIEVED CONTENT ===\n\n");

                        String formattedContent = contentBuilder.toString();
                        Log.infof("CustomContentInjector: Formatted %d content item(s), total length: %d chars",
                                        contents.size(), formattedContent.length());

                        // Get the user message text
                        String userMessageText = "";
                        if (userMessage instanceof UserMessage) {
                                userMessageText = ((UserMessage) userMessage).singleText();
                        } else {
                                userMessageText = userMessage.toString();
                        }

                        // Build the final message with clear instructions
                        String finalMessage = String.format(
                                        """
                                                        USER QUESTION: %s

                                                        Answer using the following sources. When referencing information, cite the source using markdown links in the format [source_name](source_url).

                                                        Available sources:
                                                        %s

                                                        CRITICAL INSTRUCTIONS:
                                                        - Answer the question using ONLY the information provided above in the RETRIEVED DOCUMENT CONTENT section

                                                        CITATION REQUIREMENTS (VERY IMPORTANT):
                                                        - ONLY cite sources that are EXPLICITLY shown above in the "Source:" lines
                                                        - Format your answer with inline citations like this: "According to the [Cancellation Policy](url), you can..."
                                                        - NEVER invent, make up, or guess source filenames or page numbers

                                                        - If no source is shown for information, DO NOT add a citation - just state the information without a citation
                                                        - If the retrieved content doesn't contain the answer, say: "I don't have that specific information in the retrieved documents."

                                                        - Be comprehensive and detailed (minimum 3-5 sentences)
                                                        - If information is missing, explain what is available and what might be needed

                                                        Now provide your answer as plain text:
                                                        """,
                                        userMessageText, formattedContent);

                        Log.debugf("CustomContentInjector: Final message length: %d chars", finalMessage.length());
                        return UserMessage.from(finalMessage);
                }

        }
}