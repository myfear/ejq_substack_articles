package org.acme.aiservices;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/**
 * AI service interface for generating high-level abstractions from conversation fragments.
 * 
 * <p>This service leverages Large Language Models to transform collections of related
 * conversation fragments into concise, semantically rich abstractions. The abstraction
 * process is designed to preserve essential information while eliminating redundancy
 * and temporal details, creating summaries that are useful for long-term memory
 * retrieval and context understanding.</p>
 * 
 * <p>Key abstraction principles:</p>
 * <ul>
 *   <li><strong>Condensation:</strong> Combines multiple related details into essential concepts</li>
 *   <li><strong>Generalization:</strong> Transforms specific instances into broader patterns</li>
 *   <li><strong>Deduplication:</strong> Removes redundant or trivial information</li>
 *   <li><strong>Semantic Preservation:</strong> Retains only the most semantically important elements</li>
 * </ul>
 * 
 * <p>The service is particularly effective at:</p>
 * <ul>
 *   <li>Combining similar concepts (e.g., "enjoys outdoor activities" vs. listing specific activities)</li>
 *   <li>Preserving critical identifiers (names, locations) while generalizing contexts</li>
 *   <li>Creating 1-2 sentence summaries that capture essence without losing meaning</li>
 *   <li>Focusing on information likely to be relevant for future queries</li>
 *   <li>Eliminating temporal details unless they reveal important patterns</li>
 * </ul>
 * 
 * <p>This service is a core component of the hierarchical memory system, enabling
 * the creation of multi-level abstractions that maintain semantic coherence while
 * dramatically reducing storage requirements and improving retrieval efficiency.</p>
 * 
 * @author AI Memory System
 * @since 1.0
 */
@RegisterAiService
public interface AbstractionAiService {

    /**
     * Generates concise, high-level abstractions from conversation fragments.
     * 
     * <p>This method processes a collection of related conversation fragments and
     * creates intelligent abstractions that preserve essential semantic information
     * while eliminating redundancy and temporal noise. The abstraction process
     * follows sophisticated guidelines to ensure maximum utility for future
     * memory retrieval operations.</p>
     * 
     * <p>Abstraction process:</p>
     * <ol>
     *   <li><strong>Content Analysis:</strong> Analyzes fragments for semantic relationships</li>
     *   <li><strong>Concept Extraction:</strong> Identifies key concepts and patterns</li>
     *   <li><strong>Information Consolidation:</strong> Combines related information</li>
     *   <li><strong>Redundancy Elimination:</strong> Removes duplicate or trivial content</li>
     *   <li><strong>Summary Generation:</strong> Creates concise, meaningful summaries</li>
     * </ol>
     * 
     * <p>The generated abstractions are optimized for:</p>
     * <ul>
     *   <li>Long-term memory storage efficiency</li>
     *   <li>Semantic search and retrieval</li>
     *   <li>Context understanding in future conversations</li>
     *   <li>Pattern recognition across conversation history</li>
     * </ul>
     * 
     * <p>Quality characteristics of generated abstractions:</p>
     * <ul>
     *   <li><strong>Conciseness:</strong> Typically 1-2 sentences per abstraction</li>
     *   <li><strong>Completeness:</strong> Preserves all semantically important information</li>
     *   <li><strong>Coherence:</strong> Maintains logical relationships between concepts</li>
     *   <li><strong>Contextual Relevance:</strong> Focuses on information useful for future queries</li>
     * </ul>
     * 
     * @param conversationFragments collection of related conversation fragments to abstract
     * @return concise abstractions, each as a brief, essential summary preserving key semantic content
     */
    @UserMessage("""
        Create concise, high-level abstractions from the following conversation fragments. Each abstraction should:
        
        1. Condense multiple related details into essential concepts
        2. Generalize specific instances into broader patterns
        3. Remove redundant or trivial information
        4. Preserve only the most semantically important elements
        
        Guidelines:
        - Combine similar concepts (e.g., "enjoys outdoor activities" instead of listing specific activities)
        - Keep critical identifiers (names, locations) but generalize contexts
        - Create 1-2 sentence summaries that capture the essence
        - Focus on what's likely to be relevant for future queries
        - Eliminate temporary details (like "tomorrow's tasks") unless they reveal patterns
        
        Conversation fragments:
        {conversationFragments}
        
        Provide concise abstractions, each as a brief, essential summary.
        """)
    String summarize(String conversationFragments);

}
