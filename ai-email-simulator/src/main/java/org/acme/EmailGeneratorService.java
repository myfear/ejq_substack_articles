package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(modelName = "generator")
public interface EmailGeneratorService {

    @SystemMessage("""
            Create a short internal email. Format it with:
            - a subject line
            - a body that either includes an action request or shares team information.
            Keep the tone casual and professional.
            Use no more than 100 words.
            Do not use markdown. Do not explain the prompt.
            Just return the email content starting with "Subject:"
            """)
    String generateEmail(@UserMessage String projectname);
}