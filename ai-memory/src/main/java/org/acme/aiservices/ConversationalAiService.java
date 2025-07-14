package org.acme.aiservices;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import java.util.UUID;

/**
 * AI service interface for memory-enhanced conversational interactions.
 * 
 * <p>This service provides the core conversational AI capabilities with access to
 * a sophisticated long-term memory system. The service combines Large Language Model
 * capabilities with intelligent memory retrieval to deliver contextually aware,
 * personalized responses that draw from previous conversational history.</p>
 * 
 * <p>Key capabilities:</p>
 * <ul>
 *   <li><strong>Memory-Enhanced Responses:</strong> Integrates relevant memories into responses</li>
 *   <li><strong>Contextual Understanding:</strong> Maintains awareness of past interactions</li>
 *   <li><strong>Personalized Interactions:</strong> Adapts responses based on user history</li>
 *   <li><strong>Multi-Conversation Support:</strong> Handles multiple concurrent conversation threads</li>
 * </ul>
 * 
 * <p>The service architecture integrates several sophisticated components:</p>
 * <ul>
 *   <li><strong>Retrieval Augmentation:</strong> Automatically retrieves relevant memories</li>
 *   <li><strong>Semantic Memory Search:</strong> Uses embedding-based similarity matching</li>
 *   <li><strong>Context Assembly:</strong> Combines retrieved memories into coherent context</li>
 *   <li><strong>Response Generation:</strong> Produces informed, natural language responses</li>
 * </ul>
 * 
 * <p>The memory system enables the AI to:</p>
 * <ul>
 *   <li>Remember user preferences and characteristics</li>
 *   <li>Maintain context across conversation sessions</li>
 *   <li>Reference previous discussions and decisions</li>
 *   <li>Provide personalized recommendations and insights</li>
 * </ul>
 * 
 * <p>This service is configured with {@link MemoryRetrievalAugmentorSupplier} to
 * provide automatic memory retrieval and context augmentation for all conversations.</p>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@RegisterAiService(retrievalAugmentor = MemoryRetrievalAugmentorSupplier.class)
public interface ConversationalAiService {

    /**
     * Conducts a memory-enhanced conversation with the AI assistant.
     * 
     * <p>This method processes user queries and generates responses that are informed
     * by relevant memories from the long-term memory system. The conversation process
     * involves sophisticated memory retrieval and context integration to provide
     * personalized, contextually aware responses.</p>
     * 
     * <p>Conversation process:</p>
     * <ol>
     *   <li><strong>Query Processing:</strong> Analyzes the user's input for semantic content</li>
     *   <li><strong>Memory Retrieval:</strong> Searches for relevant memories using embedding similarity</li>
     *   <li><strong>Context Integration:</strong> Assembles retrieved memories into coherent context</li>
     *   <li><strong>Response Generation:</strong> Produces AI responses informed by memory context</li>
     *   <li><strong>Memory Storage:</strong> Stores the conversation turn for future reference</li>
     * </ol>
     * 
     * <p>Memory retrieval features:</p>
     * <ul>
     *   <li>Semantic similarity matching using high-dimensional embeddings</li>
     *   <li>Cluster-aware retrieval for finding related memory groups</li>
     *   <li>Multi-factor ranking based on relevance, recency, and importance</li>
     *   <li>Access pattern tracking for improved future retrievals</li>
     * </ul>
     * 
     * <p>Response characteristics:</p>
     * <ul>
     *   <li><strong>Contextual Awareness:</strong> Incorporates relevant historical context</li>
     *   <li><strong>Personalization:</strong> Adapts to user preferences and patterns</li>
     *   <li><strong>Conciseness:</strong> Provides helpful, focused responses</li>
     *   <li><strong>Coherence:</strong> Maintains logical flow and consistency</li>
     * </ul>
     * 
     * <p>The conversation ID enables:</p>
     * <ul>
     *   <li>Isolation between different conversation threads</li>
     *   <li>Conversation-specific memory associations</li>
     *   <li>Multi-user support with separate contexts</li>
     *   <li>Persistent conversation history</li>
     * </ul>
     * 
     * @param conversationId unique identifier for the conversation thread
     * @param query the user's input message or question
     * @return AI-generated response enhanced with relevant memory context
     */
    @SystemMessage("""
        You are a helpful personal assistant.
        You have access to a long-term memory containing facts about the user.
        Use the information from the 'Relevant Memories' section to answer the user's questions.
        Be concise and helpful.
        """)
    String chat(@MemoryId UUID conversationId, @UserMessage String query);
}
