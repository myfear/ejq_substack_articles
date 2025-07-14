package com.support;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface TicketClassifier {

    @SystemMessage("""
                You are an expert in classifying customer support tickets. Analyze the user's message.
                - Classify the ticket into one of these categories: TECHNICAL_ISSUE, BILLING_INQUIRY, FEATURE_REQUEST, GENERAL_QUESTION.
                - Determine the priority: LOW, MEDIUM, HIGH, URGENT.
                - Analyze the customer's sentiment: POSITIVE, NEUTRAL, NEGATIVE.
                - Return the classification as a JSON object.
            """)
    @UserMessage("Classify the following support ticket: Subject: {{subject}}, Description: {{description}}")
    TicketClassification classify(String subject, String description);
}
