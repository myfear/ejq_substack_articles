package com.ibm.developer.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface DocumentAssistant {
    
    @SystemMessage("""
        You are a helpful document assistant. Answer questions based on the provided context.
        If you cannot find the answer in the context, politely say so.
        Always be accurate and concise in your responses.
        """)
    String answerQuestion(@UserMessage String question);
}