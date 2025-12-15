package com.ibm.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.ibm.ai.SalesEnablementBot;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.ContentMetadata;
import dev.langchain4j.service.Result;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/bot")
@Authenticated
public class SalesEnablementResource {

    public record SourceLink(String fileName, String page, String url) {}
    public record ChatResponse(String answer, List<SourceLink> sources) {}

    @Inject
    SalesEnablementBot bot;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ChatResponse ask(@QueryParam("q") String question) {
        if (question == null || question.trim().isEmpty()) {
            question = "What is the best solution for a client who is migrating to a microservices architecture?";
        }
        Result<String> result = bot.chat(question);

        List<SourceLink> citations = result.sources().stream()
                .map(this::toSourceLink)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return new ChatResponse(result.content(), citations);
        //return new BotResponse(botResponse);
    }

    private SourceLink toSourceLink(Content content) {
        Map<ContentMetadata, Object> metadata = content.metadata();
        String fileName = getMetadataValue(metadata, "file_name");
        String page = getMetadataValue(metadata, "page_number");
        String url = getMetadataValue(metadata, "url");

        // Database records or other content might not have URLs
        if (url == null || url.isBlank()) {
            return null;
        }
        return new SourceLink(fileName, page, url);
    }

    private String getMetadataValue(Map<ContentMetadata, Object> metadata, String keyName) {
        if (metadata == null) {
            return null;
        }
        return metadata.entrySet().stream()
                .filter(entry -> entry.getKey().toString().equals(keyName))
                .map(entry -> entry.getValue() != null ? entry.getValue().toString() : null)
                .findFirst()
                .orElse(null);
    }


}

