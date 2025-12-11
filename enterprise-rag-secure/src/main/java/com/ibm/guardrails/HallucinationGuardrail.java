package com.ibm.guardrails;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;

/**
 * HallucinationGuardrail detects when the LLM generates responses that:
 * - Admit lack of knowledge
 * - Are too vague or generic
 * - Contain contradictory information
 * - Make up facts not present in the CloudX sales enablement materials
 * - Provide overly confident answers without proper context
 */
@ApplicationScoped
public class HallucinationGuardrail implements OutputGuardrail {

    // Phrases indicating the model doesn't have information
    private static final String[] UNCERTAINTY_PHRASES = {
            "i don't have that information",
            "i don't know",
            "i'm not sure",
            "i cannot find",
            "i don't have access to",
            "i'm unable to provide",
            "i don't have specific information",
            "i cannot confirm",
            "i'm not aware of",
            "i don't have details about"
    };

    // Phrases indicating potential hallucination or making up information
    private static final String[] HALLUCINATION_INDICATORS = {
            "as far as i know",
            "i believe",
            "i think",
            "probably",
            "it seems like",
            "it appears that",
            "i assume",
            "i would guess",
            "most likely",
            "presumably"
    };

    // Contradictory phrases that might indicate confusion
    private static final String[] CONTRADICTION_INDICATORS = {
            "however, on the other hand",
            "but actually",
            "or maybe",
            "alternatively, it could be",
            "i'm not certain, but"
    };

    // CloudX-specific facts that should be accurate
    private static final String[][] CLOUDX_FACTS = {
            // Format: {incorrect_value, correct_value, context}
            { "99.9% uptime", "99.99%", "enterprise tier" },
            { "$599", "$499", "starter tier monthly" },
            { "$2,999", "$1,999", "professional tier monthly" },
            { "aws only", "aws, azure, and google cloud", "multi-cloud support" },
            { "competecloud is cheaper", "cloudx is 8% lower for enterprise", "enterprise pricing" }
    };

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        Log.info("HallucinationGuardrail: Validating LLM response");

        String content = responseFromLLM.text();
        String contentLower = content.toLowerCase();
        Log.debug("HallucinationGuardrail: Response content length: " + content.length() + " characters");

        // 1. Check for uncertainty phrases (model admitting it doesn't know)
        String uncertaintyPhrase = detectUncertaintyPhrase(contentLower);
        if (uncertaintyPhrase != null) {
            Log.warn("HallucinationGuardrail: Detected uncertainty phrase: '" + uncertaintyPhrase + "'");
            return reprompt(
                    "The response contains uncertainty phrases. ",
                    "Please provide a confident answer based strictly on the CloudX sales enablement materials. " +
                            "If the information is not available in the provided documents, clearly state that the information is not in the available materials rather than expressing uncertainty.");
        }

        // 2. Check for hallucination indicators (hedging language suggesting
        // uncertainty)
        String hallucinationIndicator = detectHallucinationIndicator(contentLower);
        if (hallucinationIndicator != null) {
            Log.warn("HallucinationGuardrail: Detected hallucination indicator: '" + hallucinationIndicator + "'");
            return reprompt(
                    "The response contains hedging language that suggests uncertainty. ",
                    "Please provide a confident, fact-based answer using only information from the CloudX sales enablement materials. "
                            +
                            "If the information is not in the documents, clearly state that the information is not available rather than speculating or using uncertain language.");
        }

        // 3. Check for contradictory statements
        String contradictionIndicator = detectContradictionIndicator(contentLower);
        if (contradictionIndicator != null) {
            Log.warn("HallucinationGuardrail: Detected contradiction indicator: '" + contradictionIndicator + "'");
            return reprompt(
                    "The response contains contradictory or conflicting statements. ",
                    "Please provide a clear, consistent answer based on the CloudX sales enablement materials. "
                            +
                            "Ensure all information is coherent and does not present conflicting details.");
        }

        // 4. Check for too short/lazy answers
        if (content.trim().length() < 20) {
            Log.warn("HallucinationGuardrail: Response too short - " + content.trim().length() + " characters");
            return reprompt(
                    "The response is too brief and lacks sufficient detail. ",
                    "Please provide a comprehensive response with specific details, examples, and concrete information from the CloudX sales enablement materials.");
        }

        // 5. Check for overly generic responses
        if (isOverlyGeneric(contentLower)) {
            Log.warn("HallucinationGuardrail: Response is overly generic - lacks CloudX-specific details");
            return reprompt(

                    "The response is too generic and lacks specific CloudX details. ",
                    "Please provide concrete information about CloudX features, pricing, capabilities, competitive advantages, "
                            +
                            "or specific use cases from the sales enablement materials. Include specific product names, pricing tiers, percentages, or technical details where relevant.");
        }

        // 6. Check for potential factual errors about CloudX
        String factualError = detectFactualError(contentLower);
        if (factualError != null) {
            Log.warn("HallucinationGuardrail: Detected potential factual error: " + factualError);
            return reprompt(
                    "The response may contain a factual error: " + factualError + ". ",
                    "Please carefully verify all information against the CloudX sales enablement materials and provide accurate, verified details. "
                            +
                            "Only include information that is explicitly stated in the provided documents.");
        }

        // 7. Check for excessive hedging (multiple uncertainty markers)
        int hedgingCount = countHedgingPhrases(contentLower);
        if (hedgingCount >= 3) {
            Log.warn("HallucinationGuardrail: Excessive hedging detected - " + hedgingCount + " hedging phrases found");
            return reprompt(
                    "The response contains excessive hedging language that suggests uncertainty. ",
                    "Please provide a confident, fact-based answer using information directly from the CloudX sales enablement materials. "
                            +
                            "Avoid hedging phrases and present information with confidence when it is supported by the documents.");
        }

        // All checks passed
        Log.info("HallucinationGuardrail: Response validated successfully - no hallucination indicators detected");
        return success();
    }

    private String detectUncertaintyPhrase(String content) {
        for (String phrase : UNCERTAINTY_PHRASES) {
            if (content.contains(phrase)) {
                return phrase;
            }
        }
        return null;
    }

    private String detectHallucinationIndicator(String content) {
        for (String indicator : HALLUCINATION_INDICATORS) {
            if (content.contains(indicator)) {
                return indicator;
            }
        }
        return null;
    }

    private String detectContradictionIndicator(String content) {
        for (String indicator : CONTRADICTION_INDICATORS) {
            if (content.contains(indicator)) {
                return indicator;
            }
        }
        return null;
    }

    private boolean isOverlyGeneric(String content) {
        // Check if response lacks specific CloudX details
        String[] specificKeywords = {
                "cloudx", "starter tier", "professional tier", "enterprise tier",
                "$499", "$1,999", "$5,999", "99.99%", "multi-cloud",
                "competecloud", "skyplatform", "techgiant",
                "kubernetes", "aws", "azure", "google cloud"
        };

        int specificCount = 0;
        for (String keyword : specificKeywords) {
            if (content.contains(keyword)) {
                specificCount++;
            }
        }

        // If response is longer than 100 chars but has no specific CloudX details, it's
        // too generic
        return content.length() > 100 && specificCount == 0;
    }

    private String detectFactualError(String content) {
        // Check for common factual errors about CloudX
        for (String[] fact : CLOUDX_FACTS) {
            String incorrectValue = fact[0];
            String correctValue = fact[1];
            String context = fact[2];

            if (content.contains(incorrectValue)) {
                return "Found '" + incorrectValue + "' but the correct value is '" + correctValue + "' for " + context;
            }
        }
        return null;
    }

    private int countHedgingPhrases(String content) {
        int count = 0;
        String[] hedgingPhrases = {
                "might", "maybe", "perhaps", "possibly", "could be",
                "may be", "seems", "appears", "likely", "probably"
        };

        for (String phrase : hedgingPhrases) {
            if (content.contains(phrase)) {
                count++;
            }
        }
        return count;
    }
}