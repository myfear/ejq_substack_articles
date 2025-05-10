package org.acme;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.SessionScoped;

@RegisterAiService
@SessionScoped
public interface SessionScopeChatAgent {
    String chat(@UserMessage String userMessage);
}
