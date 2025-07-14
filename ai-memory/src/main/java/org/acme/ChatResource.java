package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.UUID;

import org.acme.aiservices.ConversationalAiService;

/**
 * REST endpoint for conversational AI interactions with memory-enhanced responses.
 * 
 * <p>This resource provides HTTP endpoints for conducting conversations with an AI
 * assistant that has access to a sophisticated memory system. The AI can retrieve
 * and incorporate relevant memories from past conversations to provide contextually
 * aware and personalized responses.</p>
 * 
 * <p>Key capabilities:</p>
 * <ul>
 *   <li><strong>Memory-Enhanced Chat:</strong> AI responses informed by relevant memories</li>
 *   <li><strong>Conversation Tracking:</strong> Support for multiple concurrent conversations</li>
 *   <li><strong>Context Retrieval:</strong> Automatic retrieval of relevant historical context</li>
 *   <li><strong>Semantic Understanding:</strong> Leverages embedding-based similarity for memory recall</li>
 * </ul>
 * 
 * <p>The conversation system integrates with:</p>
 * <ul>
 *   <li>Memory retrieval service for context gathering</li>
 *   <li>Clustering system for semantic memory organization</li>
 *   <li>LangChain4j AI services for natural language processing</li>
 *   <li>Memory processing pipeline for storing new conversational content</li>
 * </ul>
 * 
 * <p>Each conversation is identified by a unique UUID, allowing for multiple
 * concurrent conversations while maintaining separate context and memory
 * associations for each interaction thread.</p>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@Path("/chat")
public class ChatResource {

    @Inject
    ConversationalAiService aiService;

    /**
     * Conducts a conversational exchange with memory-enhanced AI responses.
     * 
     * <p>This endpoint processes user queries and generates AI responses that are
     * informed by relevant memories from the memory system. The conversation process
     * involves several sophisticated steps:</p>
     * 
     * <ol>
     *   <li><strong>Query Analysis:</strong> Analyzes the incoming user query for semantic content</li>
     *   <li><strong>Memory Retrieval:</strong> Searches for relevant memories using embedding similarity</li>
     *   <li><strong>Context Assembly:</strong> Combines retrieved memories into coherent context</li>
     *   <li><strong>Response Generation:</strong> Uses AI service to generate contextually aware responses</li>
     *   <li><strong>Memory Storage:</strong> Stores the conversation turn for future retrieval</li>
     * </ol>
     * 
     * <p>Memory retrieval features:</p>
     * <ul>
     *   <li>Semantic similarity matching using embedding vectors</li>
     *   <li>Cluster-aware retrieval for finding related memory groups</li>
     *   <li>Intelligent ranking based on relevance, recency, and importance</li>
     *   <li>Access pattern tracking for improving future retrievals</li>
     * </ul>
     * 
     * <p>The conversation ID allows for:</p>
     * <ul>
     *   <li>Maintaining separate conversation threads</li>
     *   <li>Context continuity within conversations</li>
     *   <li>Conversation-specific memory associations</li>
     *   <li>Multi-user support and isolation</li>
     * </ul>
     * 
     * @param conversationId unique identifier for the conversation thread
     * @param query the user's input query or message
     * @return AI-generated response enhanced with relevant memory context
     */
    @POST
    @Path("/{conversationId}")
    public String chatWithMemory(@PathParam("conversationId") UUID conversationId, String query) {
        return aiService.chat(conversationId, query);
    }
}
