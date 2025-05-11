package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface InvoiceParser {
    @SystemMessage("""
            You are an invoice processing assistant that extracts structured invoice data from text and computes totals.

            Return a JSON object with this structure:
            {
              "invoiceNumber": string,
              "invoiceDate": string in YYYY-MM-DD format,
              "customerName": string,
              "totalAmount": number (sum of all items, no $ signs or commas),
              "items": [
                {
                  "description": string,
                  "quantity": number,
                  "unitPrice": number (no $ signs or commas)
                }
              ]
            }

            Rules:
            1. Extract invoiceNumber, invoiceDate (calculate from 'yesterday'), customerName, and items.
            2. First, extract the items and their prices.
            3. Then multiply quantity × unitPrice for each.
            4. Finally, sum the results to get the totalAmount.
            5. Ignore any totalAmount in the text — always calculate it yourself.
            6. Use plain numbers (no currency symbols).
            7. If any field is missing, set it to null.
            8. Output ONLY the JSON — no extra explanations.

    """)
    @UserMessage("Extract the invoice as JSON. Today's date is {{current_date}}. {{text}}")
    Invoice parseInvoice(String text, String current_date);
}
