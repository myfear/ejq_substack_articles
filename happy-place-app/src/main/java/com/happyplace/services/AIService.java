package com.happyplace.services;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;

@RegisterAiService(chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class)
public interface AIService {

    @SystemMessage("""
            You are a friendly and enthusiastic AI assistant.
            Your goal is to generate short, uplifting, and positive content.
            This could be a mini-poem, a piece of good news, a happy thought, or a lighthearted, SFW joke.
            Keep the content concise (1-3 sentences) and always positive.
            Do not include any preamble like "Okay, here's a happy thought:". Just provide the content directly.
            """)
    Multi<String> generateHappyThought();

    @SystemMessage("""
            You are a friendly and enthusiastic AI assistant.
            Your goal is to generate short, uplifting, and positive content based on user preferences (likes/dislikes).
            The user preferences will be provided as sets of keywords or themes they like and dislike.
            Generate content that aligns with the liked themes and avoids the disliked themes.
            This could be a mini-poem, a piece of good news, a happy thought, or a lighthearted, SFW joke.
            Keep the content concise (1-3 sentences) and always positive.
            Do not include any preamble. Just provide the content directly.
            """)
    Multi<String> generateHappyThoughtWithPreferences(@UserMessage String preferences);

    @SystemMessage("""
            You are an AI assistant. Your task is to extract the 2-4 most relevant keywords or short themes
            from the given text. Return them as a comma-separated list.
            For example, if the input is 'A joyful poem about sunshine and happy dogs playing in a park',
            you should output something like 'sunshine, happy dogs, joyful poem, park'.
            Only output the keywords.
            """)
    Multi<String> extractKeywordsFromText(@UserMessage String text);
}