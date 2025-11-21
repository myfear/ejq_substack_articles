package com.example;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@RegisterAiService
@ApplicationScoped
public interface EmotionAgent {

    @SystemMessage("""
            You are an expert facial expression analyst. Analyze the facial expression in the image and respond with ONLY a single emotion word.
            """)
    String detect(
            @UserMessage("What emotion is shown in this face image? Respond with only one word: happy, sad, angry, fearful, disgusted, surprised, or neutral.") Image faceImage);
}