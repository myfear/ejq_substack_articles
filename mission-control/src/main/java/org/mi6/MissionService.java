package org.mi6;


import io.quarkiverse.presidio.runtime.Analyzer;
import io.quarkiverse.presidio.runtime.Anonymizer;
import io.quarkiverse.presidio.runtime.model.AnalyzeRequest;
import io.quarkiverse.presidio.runtime.model.AnonymizeRequest;
import io.quarkiverse.presidio.runtime.model.AnonymizeResponse;
import io.quarkiverse.presidio.runtime.model.Mask;
import io.quarkiverse.presidio.runtime.model.RecognizerResultWithAnaysisExplanation;
import io.quarkiverse.presidio.runtime.model.Replace;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class MissionService {

    @Inject
    MissionGenerator model;

    @Inject
    @RestClient
    Analyzer analyzer;

    @Inject
    @RestClient
    Anonymizer anonymizer;

    // Define specific anonymization strategies for our mission briefings
    private static final Replace PERSON_REPLACE = new Replace("[AGENT_NAME]");
    private static final Mask PHONE_MASK = new Mask("*", 4, true); // Mask all but last 4 digits
    private static final Replace EMAIL_REPLACE = new Replace("[REDACTED_EMAIL]");
    private static final Replace LOCATION_REPLACE = new Replace("[CLASSIFIED_LOCATION]");
    private static final Replace DATE_REPLACE = new Replace("[CLASSIFIED_DATE]");
    private static final Replace DEFAULT_REPLACE = new Replace("*****");

    /**
     * A record to hold all the results of our operation.
     * This will be serialized to JSON and sent to the UI.
     */
    public record Mission(
            String originalText,
            String redactedText,
            List<RecognizerResultWithAnaysisExplanation> entities
    ) {}

    /**
     * Generates a new mission, analyzes it for PII, and prepares a redacted version.
     *
     * @return A Mission object containing original text, redacted text, and found entities.
     */
    public Mission generateAndAnalyzeMission() {
        // Step 0: Generate a mission from the Ollama model
        String missionText = model.generateMission("Ethan Hunt");
        

        // Step 1: Analyze the text to find all PII entities
        var analyzeRequest = new AnalyzeRequest();
        analyzeRequest.text(missionText);
        analyzeRequest.language("en");
        List<RecognizerResultWithAnaysisExplanation> recognizerResults = analyzer.analyzePost(analyzeRequest);

        // Step 2: Define the anonymization request with our custom strategies
        var anonymizeRequest = new AnonymizeRequest();
        anonymizeRequest.setText(missionText);
        anonymizeRequest.analyzerResults(Collections.unmodifiableList(recognizerResults));

        // Apply our specific strategies
        anonymizeRequest.putAnonymizersItem("DEFAULT", DEFAULT_REPLACE);
        anonymizeRequest.putAnonymizersItem("PERSON", PERSON_REPLACE);
        anonymizeRequest.putAnonymizersItem("LOCATION", LOCATION_REPLACE);
        anonymizeRequest.putAnonymizersItem("PHONE_NUMBER", PHONE_MASK);
        anonymizeRequest.putAnonymizersItem("EMAIL_ADDRESS", EMAIL_REPLACE);
        anonymizeRequest.putAnonymizersItem("DATE_TIME", DATE_REPLACE);

        // Step 3: Call the anonymizer to get the redacted text
        AnonymizeResponse anonymizeResponse = anonymizer.anonymizePost(anonymizeRequest);
        String anonymizedText = anonymizeResponse.getText();

        // Step 4: Package everything into our Mission record and return it
        return new Mission(missionText, anonymizedText, recognizerResults);
    }
}