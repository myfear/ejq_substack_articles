package com.ibm.guardrails;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;

/**
 * OutOfScopeGuardrail ensures the AI assistant stays within the boundaries of
 * CloudX sales enablement content and doesn't provide information outside its
 * domain.
 *
 * Based on the sales enablement resources, the scope includes:
 * - CloudX Enterprise Platform features, pricing, and capabilities
 * - Competitive analysis and positioning (based on public information)
 * - Sales methodology and processes
 * - Customer success stories and ROI information
 * - Technical architecture and supported technologies
 * - Migration strategies and implementation approaches
 *
 * Out of scope includes:
 * - Competitor internal operations or confidential information
 * - Non-CloudX IBM products or third-party services (unless in context of
 * integration/comparison)
 * - Legal, financial, tax, or investment advice
 * - Personal or non-business advice
 * - Confidential customer information or unreleased features
 * - Custom pricing negotiations (should be referred to sales team)
 * - General technology tutorials unrelated to CloudX
 */
@ApplicationScoped
public class OutOfScopeGuardrail implements OutputGuardrail {

    // Keywords indicating competitor-specific internal information (out of scope)
    private static final String[] COMPETITOR_INTERNAL_KEYWORDS = {
            "competecloud's internal", "competecloud roadmap", "competecloud strategy",
            "skyplatform's internal", "skyplatform roadmap", "skyplatform strategy",
            "techgiant's internal", "techgiant roadmap", "techgiant strategy",
            "competitor's source code", "competitor's architecture"
    };

    // Keywords indicating non-CloudX products (out of scope)
    private static final String[] NON_CLOUDX_PRODUCTS = {
            "watson", "db2", "websphere traditional", "maximo", "cognos",
            "spss", "qradar", "guardium", "appscan", "rational",
            "aws lambda", "azure functions", "google cloud run",
            "heroku", "digitalocean", "linode"
    };

    // Keywords indicating requests for confidential/inappropriate information
    private static final String[] CONFIDENTIAL_KEYWORDS = {
            "confidential customer", "internal only", "proprietary information",
            "trade secret", "non-disclosure", "customer's private",
            "competitor's financials", "unreleased feature", "beta feature"
    };

    // Keywords indicating legal/financial advice requests (out of scope)
    private static final String[] ADVICE_KEYWORDS = {
            "legal advice", "tax advice", "investment advice", "financial planning",
            "should i invest", "legal opinion", "tax implications",
            "securities advice", "compliance advice", "audit advice"
    };

    // Keywords indicating personal/non-business requests (out of scope)
    private static final String[] PERSONAL_KEYWORDS = {
            "personal recommendation", "what should i do with my career",
            "help me with my resume", "dating advice", "health advice",
            "medical advice", "therapy", "counseling"
    };

    // Keywords indicating requests for custom pricing/negotiations (should be
    // referred)
    private static final String[] NEGOTIATION_KEYWORDS = {
            "negotiate my contract", "get me a better deal", "discount my price",
            "override the pricing", "special pricing for me", "custom contract terms"
    };

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        Log.info("OutOfScopeGuardrail: Validating LLM response");

        String content = responseFromLLM.text().toLowerCase();
        Log.debug("OutOfScopeGuardrail: Response content length: " + content.length() + " characters");

        // Check for various out-of-scope categories
        String detectedIssue = detectOutOfScopeContent(content);

        if (detectedIssue != null) {
            Log.warn("OutOfScopeGuardrail: Detected out-of-scope content - Issue type: " + detectedIssue);
            return buildOutOfScopeResponse(detectedIssue);
        }

        // Response is in scope
        Log.info("OutOfScopeGuardrail: Response validated successfully - content is in scope");
        return success();
    }

    /**
     * Detects if the response contains out-of-scope content.
     * Returns a description of the issue if found, null otherwise.
     */
    private String detectOutOfScopeContent(String content) {
        // Priority order: Check most critical violations first

        // 1. Check for confidential information (highest priority)
        for (String keyword : CONFIDENTIAL_KEYWORDS) {
            if (content.contains(keyword)) {
                return "confidential";
            }
        }

        // 2. Check for legal/financial advice
        for (String keyword : ADVICE_KEYWORDS) {
            if (content.contains(keyword)) {
                return "advice";
            }
        }

        // 3. Check for personal requests
        for (String keyword : PERSONAL_KEYWORDS) {
            if (content.contains(keyword)) {
                return "personal";
            }
        }

        // 4. Check for competitor internal information
        for (String keyword : COMPETITOR_INTERNAL_KEYWORDS) {
            if (content.contains(keyword)) {
                return "competitor_internal";
            }
        }

        // 5. Check for non-CloudX products (only if not in CloudX context)
        for (String product : NON_CLOUDX_PRODUCTS) {
            if (content.contains(product) && !isCloudXContext(content)) {
                return "non_cloudx_product";
            }
        }

        // 6. Check for pricing negotiation requests
        for (String keyword : NEGOTIATION_KEYWORDS) {
            if (content.contains(keyword)) {
                return "negotiation";
            }
        }

        // 7. Check if response is about general technology not related to CloudX
        if (isGeneralTechnologyQuestion(content)) {
            return "general_technology";
        }

        return null;
    }

    /**
     * Checks if the content is discussing a product in the context of CloudX
     * (e.g., integration, comparison, migration from)
     */
    private boolean isCloudXContext(String content) {
        String[] cloudxContextKeywords = {
                "cloudx", "integrate with", "migrate from", "compared to",
                "alternative to", "replace", "modernize from"
        };

        for (String keyword : cloudxContextKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the response is about general technology topics not related to
     * CloudX
     */
    private boolean isGeneralTechnologyQuestion(String content) {
        // Check if discussing technology without CloudX context
        String[] techKeywords = {
                "how to program", "learn programming", "tutorial for",
                "what is blockchain", "what is ai", "what is machine learning",
                "how does the internet work", "what is a database"
        };

        boolean hasTechKeyword = false;
        for (String keyword : techKeywords) {
            if (content.contains(keyword)) {
                hasTechKeyword = true;
                break;
            }
        }

        // If has tech keyword but no CloudX context, it's out of scope
        return hasTechKeyword && !isCloudXContext(content);
    }

    /**
     * Builds an appropriate out-of-scope response based on the detected issue.
     * Uses reprompt() to guide the LLM to provide a better, in-scope response.
     */
    private OutputGuardrailResult buildOutOfScopeResponse(String issueType) {
        Log.info("OutOfScopeGuardrail: Building reprompt response for issue type: " + issueType);

        String userMessage;
        String repromptMessage;

        switch (issueType) {
            case "confidential":
                userMessage = "The response contains references to confidential or proprietary information. ";
                repromptMessage = "Please provide a response that only uses publicly available information from the CloudX sales enablement materials. "
                        +
                        "Focus on CloudX features, pricing, competitive positioning, and sales methodology without revealing confidential details.";
                break;

            case "advice":
                userMessage = "The response appears to provide legal, financial, or investment advice. ";
                repromptMessage = "Please reframe the response to focus on CloudX's business value, ROI calculations, and pricing structure "
                        +
                        "without providing specific legal or financial advice. Suggest consulting appropriate advisors for such matters.";
                break;

            case "personal":
                userMessage = "The response addresses personal or non-business matters.";
                repromptMessage = "Please provide a response focused on CloudX sales enablement topics such as product features, "
                        +
                        "pricing, competitive analysis, sales methodology, or customer success stories.";
                break;

            case "competitor_internal":
                userMessage = "The response discusses competitors' internal strategies or confidential information.";
                repromptMessage = "Please limit the response to publicly available competitive comparisons based on the CloudX sales enablement materials. "
                        +
                        "Focus on how CloudX compares to competitors using public information and customer feedback.";
                break;

            case "non_cloudx_product":
                userMessage = "The response discusses products or services outside of CloudX Enterprise Platform. ";
                repromptMessage = "Please focus the response on CloudX-specific features, capabilities, and use cases. "
                        +
                        "If mentioning other products, only do so in the context of CloudX integration, migration, or comparison.";
                break;

            case "negotiation":
                userMessage = "The response attempts to negotiate specific pricing or contract terms. ";
                repromptMessage = "Please provide information about standard CloudX pricing tiers, discount guidelines, and the general pricing framework. "
                        +
                        "Indicate that specific negotiations should be handled by the sales manager and deal desk team.";
                break;

            case "general_technology":
                userMessage = "The response discusses general technology topics not related to CloudX. ";
                repromptMessage = "Please refocus the response on CloudX Enterprise Platform and its applications. " +
                        "Connect the technology discussion to CloudX use cases, deployment scenarios, or architecture if relevant.";
                break;

            default:
                userMessage = "The response appears to be outside the scope of CloudX sales enablement. ";
                repromptMessage = "Please provide a response focused on CloudX Enterprise Platform features, pricing, competitive analysis, "
                        +
                        "sales methodology, or customer success stories based on the available sales enablement materials.";
        }

        // Use reprompt() with both user message and system reprompt instruction
        Log.debug("OutOfScopeGuardrail: Reprompting with user message: " + userMessage);
        return reprompt(userMessage, repromptMessage);
    }
}