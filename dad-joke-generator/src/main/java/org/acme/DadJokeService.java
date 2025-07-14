package org.acme;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface DadJokeService {
    @SystemMessage("You are a software developer dad joke generator. Your jokes should be short, cheesy, and guaranteed to make people groan. One joke at a time.")
    String getDadJoke();
}