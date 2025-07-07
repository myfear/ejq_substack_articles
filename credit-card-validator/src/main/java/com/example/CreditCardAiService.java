package com.example;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = CreditCardTools.class)
public interface CreditCardAiService {

    @SystemMessage("""
            You are a helpful assistant for validating credit card details.
            You have access to two tools:
            1. A tool to validate the credit card NUMBER and find its brand.
            2. A tool to check the card's CVC and expiration date. To use this tool, you MUST provide it with the credit card number, the CVC, and the expiration date.

            When a user asks to validate their card, you must gather the card number, the expiration date, and the CVC from their query.
            You must call BOTH tools to perform a full validation.
            When calling the CVC validation tool, you must pass the card number to it.
            Finally, combine the results from both tools into a single, comprehensive summary for the user.
            If any part of the validation fails, clearly state the reason for the failure.
            """)
    String chat(@UserMessage String userMessage);
}