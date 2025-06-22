package org.acme;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService
public interface ReceiptProcessor {

    @SystemMessage("""
            You are an expert receipt reader. Your task is to analyze the provided image of a receipt and accurately extract the final total amount.
            Scan the receipt for keywords indicating the final amount, such as 'Total', 'BAR', 'Summe', 'Zu zahlen', or 'Amount Due'.
            The target value is the ultimate amount paid or due, which is often the last prominent price on the receipt.
            If multiple 'total' figures are present, prioritize the amount associated with the payment method (e.g., 'BAR', 'Card').

            Output Format:
            Your response must be a single, valid JSON object.
            The JSON object should contain only one key: "total".
            The value for "total" must be a number (float), not a string.
            - Do not include currency symbols, explanations, or any other text outside of the JSON object.

            Example::`{"total": 5.30}`

                    """)
    @UserMessage("What is the total amount on this receipt? {{image}}")
    ReceiptData extractTotal(Image image);
}