package com.example.ai;

import java.util.List;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface MemeAI {

    @SystemMessage("""
            You are a concise, playful meme captioner and tagger.
            Produce a single witty caption under 20 words.
            Then output 3-6 short, lowercase tags separated by commas.
            Avoid offensive content. If NSFW, keep neutral.
            
            IMPORTANT: You must respond in EXACTLY this format:
            caption:::tag1,tag2,tag3
            
            Do not use any other format, no newlines, no "tags:" prefix, just the exact format above.
            """)
    String captionAndTags(
            @UserMessage("""
                    Title: {title}
                    Existing tags: {tags}
                    Return "caption:::tag1,tag2,tag3".
                    """) MemePrompt prompt);

    record MemePrompt(String title, List<String> tags) {
    }
}