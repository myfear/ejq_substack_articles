package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface SentimentAnalyzer {

    @SystemMessage("You are a sentiment analyzer. Classify the following text as POSITIVE, NEGATIVE, or NEUTRAL.")
    String analyzeSentiment(@UserMessage String text);
}
