package com.ibm.developer.service;

import static dev.langchain4j.data.document.splitter.DocumentSplitters.recursive;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 * Service responsible for ingesting documents into the vector embedding store at application startup.
 * 
 * <p>This service automatically loads documents from a configured directory, splits them into
 * smaller chunks, generates embeddings using the configured embedding model, and stores them
 * in the vector database. This process occurs once during application startup to ensure the
 * AI assistant has access to the document knowledge base.</p>
 * 
 * <p>The document splitting strategy uses recursive chunking with a chunk size of 100 tokens
 * and an overlap of 25 tokens to maintain context continuity between chunks.</p>
 * 
 * @author IBM Developer
 * @version 1.0.0
 */
@ApplicationScoped
public class DocumentIngestionService {

    /**
     * Ingests documents from the file system into the embedding store during application startup.
     * 
     * <p>This method is automatically triggered when the application starts via the CDI
     * {@code @Observes} mechanism. It performs the following steps:</p>
     * <ol>
     *   <li>Clears the existing embedding store (for demo purposes)</li>
     *   <li>Loads all documents recursively from the configured directory</li>
     *   <li>Splits documents into smaller chunks with overlap</li>
     *   <li>Generates vector embeddings for each chunk</li>
     *   <li>Stores the embeddings in the vector database</li>
     * </ol>
     * 
     * <p><strong>Note:</strong> The store is cleared on each startup for demonstration purposes.
     * In production, you may want to remove this behavior to preserve existing embeddings.</p>
     * 
     * @param ev the startup event that triggers this method
     * @param store the embedding store where document vectors will be stored
     * @param embeddingModel the model used to generate embeddings from document chunks
     * @param documents the file system path containing documents to ingest, configured via
     *                  the {@code rag.location} property
     */
    public void ingest(@Observes StartupEvent ev,
                       EmbeddingStore store, EmbeddingModel embeddingModel,
                       @ConfigProperty(name = "rag.location") Path documents) {
        store.removeAll(); // cleanup the store to start fresh (just for demo purposes)
        List<Document> list = FileSystemDocumentLoader.loadDocumentsRecursively(documents);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(recursive(100, 25))
                .build();
        ingestor.ingest(list);
        Log.info("Documents ingested successfully");
    }
}