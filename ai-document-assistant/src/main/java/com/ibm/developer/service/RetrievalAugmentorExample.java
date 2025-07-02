package com.ibm.developer.service;

import java.util.function.Supplier;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RetrievalAugmentorExample implements Supplier<RetrievalAugmentor> {

    private final RetrievalAugmentor augmentor;

    RetrievalAugmentorExample(EmbeddingStore store, EmbeddingModel model) {
        // Configure the content retriever, responsible for fetching relevant content based on the user query
        var contentRetriever = EmbeddingStoreContentRetriever.builder()
          .embeddingModel(model)
          .embeddingStore(store)
          .maxResults(3)
          .build();

        // Create the RetrievalAugmentor that combines the retriever and a default content injector
        augmentor = DefaultRetrievalAugmentor
            .builder()
            .contentRetriever(contentRetriever)
            .build();
    }

    @Override
    public RetrievalAugmentor get() {
      return augmentor;
    }

}