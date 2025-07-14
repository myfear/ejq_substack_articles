package com.support;

import java.util.List;

import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/knowledge-base/import")
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.APPLICATION_JSON)
public class KnowledgeBaseImportResource {

    @Inject
    AllMiniLmL6V2EmbeddingModel embeddingModel;

    @POST
    @Path("/generate-sql")
    public Response generateSqlImport(List<KnowledgeBaseData> articles) {
        if (articles == null || articles.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No articles provided")
                    .build();
        }

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO KnowledgeBaseArticle(id, title, content, embedding) VALUES\n");

        for (int i = 0; i < articles.size(); i++) {
            KnowledgeBaseData article = articles.get(i);

            // Generate embedding for the content
            float[] vector = embeddingModel.embed(article.content).content().vector();

            // Convert vector to PostgreSQL vector format
            String vectorString = vectorToString(vector);

            // Escape single quotes in content
            String escapedTitle = article.title.replace("'", "''");
            String escapedContent = article.content.replace("'", "''");

            sqlBuilder.append(String.format("(%d, '%s', '%s', '%s'::vector)",
                    article.id,
                    escapedTitle,
                    escapedContent,
                    vectorString));

            if (i < articles.size() - 1) {
                sqlBuilder.append(",\n");
            } else {
                sqlBuilder.append(";\n");
            }
        }

        return Response.ok(sqlBuilder.toString()).build();
    }

    @POST
    @Path("/generate-single")
    public Response generateSingleSql(KnowledgeBaseData article) {
        if (article == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("No article provided")
                    .build();
        }

        // Generate embedding for the content
        float[] vector = embeddingModel.embed(article.content).content().vector();

        // Convert vector to PostgreSQL vector format
        String vectorString = vectorToString(vector);

        // Escape single quotes in content
        String escapedTitle = article.title.replace("'", "''");
        String escapedContent = article.content.replace("'", "''");

        String sql = String.format(
                "INSERT INTO KnowledgeBaseArticle(id, title, content, embedding) VALUES\n(%d, '%s', '%s', '%s'::vector);",
                article.id,
                escapedTitle,
                escapedContent,
                vectorString);

        return Response.ok(sql).build();
    }

    private String vectorToString(float[] vector) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @GET
    @Path("/example")
    public Response getExample() {
        String exampleUsage = """
                Example usage:

                POST /api/knowledge-base/import/generate-sql
                Content-Type: application/json

                [
                  {
                    "id": 1,
                    "title": "Understanding Your Invoice",
                    "content": "Your invoice contains detailed information about your monthly charges..."
                  },
                  {
                    "id": 2,
                    "title": "Troubleshooting Startup Crashes",
                    "content": "If your application crashes during startup, check the following..."
                  }
                ]

                Or for a single article:

                POST /api/knowledge-base/import/generate-single
                Content-Type: application/json

                {
                  "id": 1,
                  "title": "Understanding Your Invoice",
                  "content": "Your invoice contains detailed information about your monthly charges..."
                }
                """;

        return Response.ok(exampleUsage).build();
    }

    public static class KnowledgeBaseData {
        public Long id;
        public String title;
        public String content;

        public KnowledgeBaseData() {
        }

        public KnowledgeBaseData(Long id, String title, String content) {
            this.id = id;
            this.title = title;
            this.content = content;
        }
    }
}
