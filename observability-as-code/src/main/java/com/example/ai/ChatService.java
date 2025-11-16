package com.example.ai;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface ChatService {

    @UserMessage("{prompt}")
    String ask(String prompt);
}