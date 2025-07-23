package com.example;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface WhitePaperAiService {

    @SystemMessage("""
            You are a professional technical writer for a technology company called 'Innovatech'.
            Your task is to generate the content for a compelling white paper.
            The tone should be professional, informative, and slightly formal.
            Generate a title, a 2-3 paragraph introduction, and 3-4 sections explaining the key features.
            Conclude with a summary paragraph. Use markdown for headings (e.g., '# Title', '## Section').
            Do not include any pre-amble or post-amble, just the white paper content itself.
            """)
    @UserMessage("""
            Generate a white paper for the product named '{{productName}}'.
            The target audience is: {{targetAudience}}.
            The key features to highlight are:
            {{features}}
            """)
    String generateWhitePaperContent(String productName, String targetAudience, String features);
}