package org.example.moderation;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface LLMModerator {

    @SystemMessage("""
            You are a highly-trained content moderation AI.
            Analyze the following text for harmful, unethical, or inappropriate content.
            Respond with a single word: either 'SAFE' or 'UNSAFE'. Do not provide any other text or explanation.
            """)
    @UserMessage("Content to analyze: {{content}}")
    String moderate(String content);
}