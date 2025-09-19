package com.ibm.txc.museum.ai;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface SentimentAi {

    @SystemMessage("""
            Classify selfie + text impression into:
            very_negative, negative, neutral, positive, very_positive.
            Only return the label.
            """)
    @UserMessage("""
            Selfie: {{image}}
            Impression: "{{impression}}"
            """)
    String classify(Image image, String impression);
}
