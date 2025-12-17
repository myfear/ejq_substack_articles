package dev.demo.toml.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface ConfigurableAiService {

    @SystemMessage("{systemPrompt}")
    @UserMessage("{userMessage}")

    String chat(String systemPrompt, String userMessage);
}