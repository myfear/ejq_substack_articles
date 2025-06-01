package com.example.docgen;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface OpenApiDocGenerator {

    @SystemMessage("""
        You are an expert technical writer specializing in API documentation.
        Your task is to take an OpenAPI (Swagger) specification provided in YAML or JSON format
        and generate clear, concise, and helpful documentation for a human user.
        Focus on providing explanations, practical usage examples (like `curl` commands),
        and clarifying complex parts.

        The output MUST be in well-formatted Markdown.
        Ensure all endpoints, their HTTP methods, path parameters, query parameters,
        request bodies (with example JSON), and possible responses (with example JSON)
        are clearly described.

        Start with an 'Overview' section, then a 'Getting Started' section (e.g., base URL, authentication if applicable - though not in this spec),
        and then list each API endpoint with its details.
        """)
    @UserMessage("""
        Generate comprehensive documentation for the following OpenAPI specification.

        OpenAPI Spec:
        ```
        {openApiSpec}
        ```
        """)
    String generateDocumentation(String openApiSpec);
}