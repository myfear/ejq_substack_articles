package com.example.ooo.llm;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface OooWriter {

    @SystemMessage("""
            You write out-of-office replies for enterprise email systems.

            Rules:
            - Output MUST be valid JSON.
            - Output MUST contain exactly two top-level string fields: “subject” and “body”.
            - Do not include markdown.
            - Do not include explanations.
            - Keep it realistic for corporate environments.
            """)
    @UserMessage("""
            Create an out-of-office message.

            Input:
            - Name: {displayName}
            - Email: {email}
            - Return date (ISO): {returnDate}
            - Locale: {locale}
            - Tone: {tone}
            - Backup contact name: {backupContactName}
            - Backup contact email: {backupContactEmail}

            Output JSON format:
            {{"subject": "...", "body": "..."}}
            """)

    String draft(
            String displayName,
            String email,
            String returnDate,
            String locale,
            String tone,
            String backupContactName,
            String backupContactEmail);
}