package com.support;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SolutionRecommendationService {

    @Inject
    AllMiniLmL6V2EmbeddingModel embeddingModel;

    @Inject
    jakarta.persistence.EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<KnowledgeBaseArticle> findSimilarSolutions(String ticketDescription) {
        Embedding embedding = embeddingModel.embed(ticketDescription).content();
        
        // Convert float array to string representation for PostgreSQL vector type
        float[] vector = embedding.vector();
        StringBuilder vectorStr = new StringBuilder();
        vectorStr.append("[");
        for (int i = 0; i < vector.length; i++) {
            vectorStr.append(vector[i]);
            if (i < vector.length - 1) {
                vectorStr.append(",");
            }
        }
        vectorStr.append("]");
        
        return entityManager.createNativeQuery(
            "SELECT * FROM KnowledgeBaseArticle ORDER BY embedding <-> CAST(:embedding AS vector) LIMIT 3",
            KnowledgeBaseArticle.class)
            .setParameter("embedding", vectorStr.toString())
            .getResultList();
    }
}
