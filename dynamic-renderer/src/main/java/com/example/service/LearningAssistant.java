package com.example.service;

import com.example.tool.JsonGuardrail;
import com.example.tool.LearningMaterialTools;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrails;

public interface LearningAssistant {

    @RegisterAiService(tools = LearningMaterialTools.class, modelName = "researcher")
    interface Researcher {
        @SystemMessage("""
                 ...
                You are a helpful assistant that recommends high-quality
                learning materials. Your task is to provide a concise summary
                in response to the user's topic or question, followed by a
                curated list of relevant books, podcasts, articles, or other resources.
                Use the webSearch tool to find up-to-date and trustworthy sources.
                Prioritize clarity, relevance, and usefulness.
                Always include links when possible, and ensure your tone
                remains professional and helpful.
                 """)
        String research(@UserMessage String question);
    }

    @RegisterAiService(modelName = "renderer") // Corresponds to 'quarkus.langchain4j.openai.renderer.*'
    @OutputGuardrails(JsonGuardrail.class) // Ensure the output is valid JSON
    interface Renderer {
        @SystemMessage("""
                ...
                Your final output must be a single JSON object with one key, \"elements\", which contains the JSON array.
                Each element in the array must be an object with two keys: \"renderHint\" (must be one of: 'text', 'book', 'podcast', 'list', 'website') and \"data\" (an object containing the actual content).

                Data structure for each renderHint:
                - 'text': { "title": string, "text": string }
                - 'book': { "title": string, "author": string }
                - 'podcast': { "title": string, "description": string }
                - 'list': { "title": string, "items": array of string }
                - 'website': { "title": string, "url": string }

                Example: { "elements": [ { "renderHint": "text", "data": {"title": "...", "text": "..."} } ] }
                Output ONLY the raw JSON object and nothing else.
                Do not use any renderHint other than those listed above.
                """)
        String render(@UserMessage String inputText);
    }
}