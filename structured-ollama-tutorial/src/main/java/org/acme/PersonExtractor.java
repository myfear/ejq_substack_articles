package org.acme;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface PersonExtractor {
    @UserMessage("Extract the name and age of the person described in the following text: {{text}}")
    Person extractPerson(String text);
}
