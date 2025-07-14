package org.acme.aiservices;

import java.util.function.Supplier;

import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MemoryRetrievalAugmentorSupplier implements Supplier<RetrievalAugmentor> {

    @Inject
    MemoryContentRetriever memoryContentRetriever;

    @Override
    public RetrievalAugmentor get() {
        return DefaultRetrievalAugmentor.builder()
                .contentRetriever(memoryContentRetriever)
                .build();
    }
} 