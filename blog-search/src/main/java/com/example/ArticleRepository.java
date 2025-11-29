package com.example;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;

@ApplicationScoped
public class ArticleRepository {

    @Inject
    EntityManager entityManager;

    public List<ArticleSearchResult> search(String query) {
        var sql = """
                SELECT a.id AS id,
                       a.title AS title,
                       a.url AS url,
                       ts_headline('english', a.content, plainto_tsquery(:q)) AS snippet,
                       ts_rank(a.search_vector, plainto_tsquery(:q)) AS rank
                FROM articles a
                WHERE a.search_vector @@ plainto_tsquery(:q)
                ORDER BY rank DESC
                LIMIT 50
                """;

        @SuppressWarnings("unchecked")
        List<Tuple> results = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("q", query)
                .getResultList();

        return results.stream().map(t -> {
            Long id = (Long) t.get("id");
            String title = (String) t.get("title");
            String url = (String) t.get("url");
            String snippet = (String) t.get("snippet");
            Number rankNum = (Number) t.get("rank");
            float rank = rankNum != null ? rankNum.floatValue() : 0.0f;
            // Calculate percentage match (ts_rank returns 0-1, convert to 0-100)
            int matchPercentage = Math.round(rank * 100);
            return new ArticleSearchResult(id, title, url, snippet, rank, matchPercentage);
        }).collect(Collectors.toList());
    }

    public void insert(Article article) {
        entityManager.persist(article);
    }

    public List<Article> recent(int limit) {
        return entityManager.createQuery("SELECT a FROM Article a ORDER BY a.createdAt DESC", Article.class)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .collect(Collectors.toList());
    }
}