package com.ibm.developer.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * AI-powered document assistant service interface using LangChain4j.
 * 
 * <p>This interface defines the contract for the AI document assistant, which leverages
 * Retrieval-Augmented Generation (RAG) to answer questions based on ingested documents.
 * The service automatically retrieves relevant document chunks from the vector store
 * and uses them as context for generating accurate, contextual answers.</p>
 * 
 * <p>The {@code @RegisterAiService} annotation automatically creates an implementation
 * of this interface at build time, wiring it with the configured LLM, embedding model,
 * and retrieval augmentor.</p>
 * 
 * <p><strong>System Behavior:</strong> The assistant is instructed to answer questions
 * based solely on the provided context and to politely indicate when information is not
 * available in the documents.</p>
 * 
 * @author IBM Developer
 * @version 1.0.0
 * @see DocumentIngestionService
 */
@RegisterAiService
public interface DocumentAssistant {
    
    /**
     * Answers a user question based on the ingested document knowledge base.
     * 
     * <p>This method uses a RAG (Retrieval-Augmented Generation) approach to:</p>
     * <ol>
     *   <li>Convert the question into a vector embedding</li>
     *   <li>Retrieve the most relevant document chunks from the embedding store</li>
     *   <li>Provide these chunks as context to the LLM</li>
     *   <li>Generate an accurate, contextual answer</li>
     * </ol>
     * 
     * <p>The system is designed to provide accurate answers based only on the available
     * documents. If the information is not found in the context, the assistant will
     * politely indicate that it cannot answer the question.</p>
     * 
     * @param question the user's question about the documents
     * @return a String containing the AI-generated answer based on document context
     */
    @SystemMessage("""
        You are a helpful document assistant. Answer questions based on the provided context.
        If you cannot find the answer in the context, politely say so.
        Always be accurate and concise in your responses.
        """)
    String answerQuestion(@UserMessage String question);
}