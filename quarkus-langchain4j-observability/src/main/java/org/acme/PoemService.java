package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface PoemService {

    @SystemMessage("You are a professional poet.")
    @UserMessage("Write a short poem about {topic}. The poem should be in the style of {style}.")
    String writePoem(String topic, String style);
}