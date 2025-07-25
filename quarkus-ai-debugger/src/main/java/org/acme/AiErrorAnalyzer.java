package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface AiErrorAnalyzer {

    @SystemMessage("""
                You are an expert Java and Quarkus developer. Your task is to analyze a Java stack trace and provide a concise, actionable solution.
                1. Identify the root cause of the error.
                2. Provide a clear explanation of why the error occurred.
                3. Offer a corrected code snippet.
                4. Respond only with the explanation and code.
            """)
    @UserMessage("""
                Analyze the following exception and provide a fix.

                Exception:
                ---
                {stackTrace}
                ---
            """)
    String analyze(String stackTrace);
}