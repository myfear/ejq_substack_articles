package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface SentimentAnalyzer {

    @SystemMessage({ "Only return the sentiment and nothing else.",
            "Here are some examples.", "This is great news!", "POSITIVE",
            "I am very happy with the service.", "POSITIVE",
            "Quarkus Dev Services are amazing and save a lot of time.", "POSITIVE",
            "Langchain4j makes LLM integration surprisingly easy.", "POSITIVE",

            "I am not happy about this situation.", "NEGATIVE",
            "The response time is too slow and frustrating.", "NEGATIVE",
            "This is a terrible experience.", "NEGATIVE",
            "The weather is miserable today.", "NEGATIVE",

            "The event is scheduled for tomorrow at 10 AM sharp.", "NEUTRAL",
            "This is a factual statement about the project configuration.", "NEUTRAL",
            "The report contains data from the last quarter.", "NEUTRAL",
            "The sky is currently overcast.", "NEUTRAL" })
    @UserMessage("Analyze sentiment of {{text}}")
    Sentiment classifySentiment(String text);

    @UserMessage("Does {{text}} have a positive sentiment?")
    boolean isPositive(String text);
}