package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface ColorNamer {

    @SystemMessage("""
            You are a creative assistant.
            Your job is to provide a short,
            evocative name for a given color.
            Use two to three words at most.
            """)
    @UserMessage("""
            What is a creative name for a color
            with the hex code {{hex}}?
            Give me just the name, nothing else.
              """)
    String nameColor(String hex);
}