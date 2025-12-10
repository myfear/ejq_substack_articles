package com.ibm.retrieval;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocumentRetriever implements RetrievalAugmentor {

    private final RetrievalAugmentor augmentor;
    private static final int SNIPPET_LENGTH = 200;

    DocumentRetriever(EmbeddingStore store, EmbeddingModel model) {
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingModel(model)
                .embeddingStore(store)
                .maxResults(3)
                .build();
        augmentor = DefaultRetrievalAugmentor
                .builder()
                .contentRetriever(contentRetriever)
                .build();
    }

    @Override
    public AugmentationResult augment(AugmentationRequest augmentationRequest) {
        // Perform the augmentation
        AugmentationResult result = augmentor.augment(augmentationRequest);

        // Log retrieved content snippets for developer visibility
        // This helps developers understand what documents are being retrieved
        var contents = result.contents();
        Log.infof("📚 DocumentRetriever: Retrieved %d document snippet(s) for augmentation", contents.size());

        for (int i = 0; i < contents.size(); i++) {
            Content content = contents.get(i);
            String text = "";
            String sourceInfo = "";

            try {
                // Content has textSegment() method that returns TextSegment
                TextSegment segment = content.textSegment();
                if (segment != null) {
                    text = segment.text();

                    // Try to extract source file information from metadata
                    var meta = segment.metadata();
                    if (meta != null) {
                        // Try to iterate over metadata entries if available
                        try {
                            // Metadata might have a way to get values - try toString for now
                            String metaString = meta.toString();
                            if (metaString.contains("file=")) {
                                // Extract file name from metadata string representation
                                int fileStart = metaString.indexOf("file=") + 5;
                                int fileEnd = metaString.indexOf(",", fileStart);
                                if (fileEnd == -1)
                                    fileEnd = metaString.indexOf("}", fileStart);
                                if (fileEnd > fileStart) {
                                    sourceInfo = " (from: " + metaString.substring(fileStart, fileEnd) + ")";
                                }
                            }
                        } catch (Exception e) {
                            // If metadata access fails, continue without source info
                            Log.debugf("Could not extract metadata: %s", e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                Log.debugf("Could not extract text from content: %s", e.getMessage());
            }

            // Create a snippet (first SNIPPET_LENGTH chars) for developer visibility
            if (!text.isEmpty()) {
                String snippet = text.length() > SNIPPET_LENGTH
                        ? text.substring(0, SNIPPET_LENGTH) + "..."
                        : text;
                // Replace newlines with spaces for cleaner log output
                snippet = snippet.replace('\n', ' ').replace('\r', ' ');
                Log.infof("  [%d] %s%s", i + 1, snippet, sourceInfo);
            } else {
                Log.infof("  [%d] (content unavailable)%s", i + 1, sourceInfo);
            }
        }

        return result;
    }

}