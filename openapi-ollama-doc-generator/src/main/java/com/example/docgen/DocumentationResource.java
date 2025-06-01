package com.example.docgen;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import io.quarkus.cache.CacheResult; // For caching

@Path("/docs")
public class DocumentationResource {

    @Inject
    OpenApiDocGenerator openApiDocGenerator; // Our LLM service

    @Inject
    @RestClient // Our REST client to fetch /q/openapi
    OpenApiRestClient openApiRestClient;

    /**
     * Generates and returns OpenAPI documentation in Markdown format.
     * Caches the result to avoid repeated LLM calls during development.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN) // Markdown is plain text
    @CacheResult(cacheName = "api-docs-cache") // Cache the generated documentation
    public Response getGeneratedDocumentation() {
        try {
            // Fetch the live OpenAPI spec from Quarkus's endpoint
            String openApiSpec = openApiRestClient.getOpenApiYaml();
            System.out.println("Fetched OpenAPI Spec (first 200 chars): "
                    + openApiSpec.substring(0, Math.min(openApiSpec.length(), 200)) + "...");

            // Use LangChain4j to generate documentation using Qwen
            String generatedDocs = openApiDocGenerator.generateDocumentation(openApiSpec);
            return Response.ok(generatedDocs).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error generating documentation: " + e.getMessage()).build();
        }
    }
}