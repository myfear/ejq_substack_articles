package org.crdt.ai;

import dev.langchain4j.service.SystemMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface AiAssistant {

    @SystemMessage("""
                You are a helpful and creative writing assistant.
                The user will provide you with a piece of text from their document.
                Your task is to continue the text with one or two concise and creative sentences.
                DO NOT repeat the user's text in your response. Just provide the new sentences.
                Your response must be plain text, without any formatting.
            """)
    String suggest(String text);
}