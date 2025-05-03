package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(modelName="processor", tools = TodoService.class)
public interface EmailProcessor {

    @SystemMessage("""
            You are an AI assistant processing incoming emails.
            If the email contains a task, call the 'addTask'
            tool with a concise description.
            Respond ONLY with ACKNOWLEDGED or THANK_YOU.
            """)
    String processEmail(@UserMessage String email);
}