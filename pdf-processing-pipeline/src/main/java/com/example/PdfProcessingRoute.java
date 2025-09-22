package com.example;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PdfProcessingRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // Global exception handling
        onException(Exception.class)
                .handled(true) // Mark as handled so it doesn't propagate
                .log(LoggingLevel.ERROR, "Error processing file: ${exception.message}")
                .setBody(constant("Error: File processing failed"))
                .stop(); // Stop further processing

        from("direct:processPdf")
                .log("New PDF received: ${header.CamelMessageBodyType}")

                .log("→ Scanning for viruses...")
                .bean("virusScanner", "scan")
                .log("✔ File is clean.")

                .log("→ Extracting text...")
                .to("pdf:extractText")
                .log("✔ Extraction done. Length: ${body}")

                .log("→ Generating summary via LLM...")
                .transform(simple(
                        "Provide a concise, one-paragraph summary of the following text extracted from a PDF file: ${body}"))
                .to("langchain4j-chat:default")
                .log("✔ Summary complete: ${body}")

                .transform(simple("PDF summary:  ${body}"))
                .log("✔ Basic processing complete:  ${header.CamelMessageBodyType}");
    }
}