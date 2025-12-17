package dev.demo.toml.api;

import java.util.Map;

import dev.demo.toml.ai.ConfigurableAiService;
import dev.demo.toml.config.AiConfiguration;
import dev.demo.toml.service.PromptComposer;
import dev.demo.toml.service.TomlConfigLoader;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/ai-config")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TomlConfigResource {

    @Inject
    TomlConfigLoader configLoader;

    @Inject
    PromptComposer promptComposer;

    @Inject
    ConfigurableAiService ai;

    @GET
    @Path("/config")
    public Map<String, Object> config() {
        AiConfiguration cfg = configLoader.getConfiguration();
        return Map.of(
                "loadedFrom", configLoader.getLoadedResourceName(),
                "title", cfg.getTitle(),
                "version", cfg.getVersion(),
                "environment", cfg.getEnvironment(),
                "model", Map.of(
                        "provider", cfg.getModel().getProvider(),
                        "name", cfg.getModel().getName(),
                        "temperature", cfg.getModel().getTemperature()),
                "features", Map.of(
                        "enableMemory", cfg.getFeatures().getEnableMemory(),
                        "enableTools", cfg.getFeatures().getEnableTools(),
                        "enableRag", cfg.getFeatures().getEnableRag()));
    }

    @POST
    @Path("/reload")
    public Map<String, Object> reload() {
        configLoader.reload();
        return Map.of("loadedFrom", configLoader.getLoadedResourceName());
    }

    @POST
    @Path("/chat")
    public ChatResponse chat(ChatRequest request) {
        AiConfiguration cfg = configLoader.getConfiguration();
        String systemPrompt = promptComposer.buildSystemPrompt(cfg);

        String answer = ai.chat(systemPrompt, request.message());
        return new ChatResponse(cfg.getModel().getName(), answer);
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String model, String response) {
    }
}