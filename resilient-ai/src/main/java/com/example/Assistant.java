package com.example;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface Assistant {

    @UserMessage("""
            Summarize the following content in 5 bullet points.
            Be concise and factual. Content: {text}
            """)
    String summarize(String text);
}