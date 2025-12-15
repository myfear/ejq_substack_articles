package com.ibm.guardrails;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.logging.Log;

/**
 * InputValidationGuardrail validates user input before it reaches the LLM.
 * It detects and blocks:
 * 1. Prompt injection attempts
 * 2. Off-topic questions outside CloudX sales enablement scope
 * 3. Malicious or inappropriate content
 * 
 * Based on CloudX sales enablement materials, valid topics include:
 * - CloudX Enterprise Platform features and capabilities
 * - Pricing and packaging information
 * - Competitive analysis and positioning
 * - Sales methodology and processes
 * - Customer success stories and ROI
 * - Technical architecture (multi-cloud, Kubernetes, supported languages)
 * - Migration and implementation strategies
 * - CloudX customer order details (e.g., ORD-123, ORD-456)
 */
@ApplicationScoped
public class InputValidationGuardrail implements InputGuardrail {

    // Prompt injection patterns
    private static final String[] PROMPT_INJECTION_PATTERNS = {
        "ignore previous instructions",
        "ignore all previous",
        "disregard previous",
        "forget previous instructions",
        "new instructions:",
        "system:",
        "you are now",
        "act as",
        "pretend you are",
        "roleplay as",
        "simulate being",
        "override your",
        "bypass your",
        "ignore your guidelines",
        "forget your role",
        "new role:",
        "system prompt:",
        "assistant:",
        "###instruction:",
        "###system:",
        "[system]",
        "<system>",
        "sudo mode",
        "developer mode",
        "jailbreak",
        "dan mode"
    };

    // Off-topic technology combinations (not supported by CloudX)
    private static final String[][] OFF_TOPIC_COMBINATIONS = {
        // Format: {technology, unsupported_context, boundary_message}
        {"python", "google cloud", "CloudX supports Python on AWS, Azure, and Google Cloud. However, I specialize in CloudX sales enablement. For deployment questions, please refer to CloudX technical documentation."},
        {"node.js", "heroku", "CloudX supports Node.js but not Heroku deployment. CloudX works with AWS, Azure, and Google Cloud."},
        {".net", "digitalocean", "CloudX supports .NET but not DigitalOcean. CloudX is designed for AWS, Azure, and Google Cloud."},
        {"ruby", "linode", "CloudX supports Ruby but not Linode. CloudX operates on AWS, Azure, and Google Cloud."}
    };

    // Topics completely outside CloudX scope
    private static final String[] COMPLETELY_OFF_TOPIC = {
        // Food & Dining
        "recipe", "cooking", "food", "restaurant", "meal", "dinner", "lunch",
        // Entertainment
        "movie", "film", "entertainment", "music", "song", "concert", "show",
        // Sports
        "sports", "football", "basketball", "soccer", "baseball", "tennis",
        // Weather & Nature
        "weather", "climate", "temperature", "forecast",
        // Health & Medical
        "health", "medical", "doctor", "medicine", "hospital", "disease",
        // Personal Life
        "dating", "relationship", "romance", "wedding", "marriage",
        // Politics & Government
        "politics", "election", "government", "president", "senator",
        // Finance (non-business)
        "cryptocurrency", "bitcoin", "blockchain", "stock market", "forex",
        // Gaming
        "gaming", "video game", "playstation", "xbox", "nintendo",
        // Travel & Booking
        "flight", "hotel", "vacation", "travel", "booking", "reservation",
        "airline", "airport", "cruise", "trip", "tourism",
        // Shopping (non-software)
        "shopping", "buy clothes", "fashion", "shoes", "jewelry",
        // Education (non-tech)
        "homework", "essay", "school assignment", "college application",
        // Real Estate
        "house", "apartment", "real estate", "mortgage", "rent",
        // Automotive
        "car", "vehicle", "automobile", "driving", "traffic"
    };

    // Action verbs for non-CloudX services
    private static final String[] OFF_TOPIC_ACTIONS = {
        "book me", "book a", "reserve a", "schedule a",
        "order me", "buy me", "purchase a",
        "find me a", "get me a",
        "recommend a restaurant", "recommend a hotel",
        "plan my trip", "plan my vacation"
    };

    // Non-CloudX products (unless in comparison/migration context)
    private static final String[] NON_CLOUDX_PRODUCTS = {
        "watson", "db2", "websphere traditional", "maximo",
        "cognos", "spss", "qradar", "guardium",
        "heroku", "digitalocean", "linode", "netlify",
        "vercel", "railway", "render"
    };

    // Malicious content indicators
    private static final String[] MALICIOUS_PATTERNS = {
        "sql injection", "drop table", "delete from",
        "script>", "<iframe", "javascript:",
        "eval(", "exec(", "system(",
        "../../../", "etc/passwd", "cmd.exe"
    };

    // Customer order ID pattern (ORD-XXX)
    private static final java.util.regex.Pattern ORDER_ID_PATTERN = 
        java.util.regex.Pattern.compile("ORD-\\d+", java.util.regex.Pattern.CASE_INSENSITIVE);

    // Customer order question indicators
    private static final String[] CUSTOMER_ORDER_KEYWORDS = {
        "customer order", "my order", "order status", "order details",
        "what is my order", "show me order", "tell me about order"
    };

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        Log.info("InputValidationGuardrail: Validating user input");
        
        String content = userMessage.singleText();
        String contentLower = content.toLowerCase();
        Log.debug("InputValidationGuardrail: Input length: " + content.length() + " characters");

        // 1. Check for prompt injection attempts (highest priority)
        String injectionPattern = detectPromptInjection(contentLower);
        if (injectionPattern != null) {
            Log.warn("InputValidationGuardrail: BLOCKED - Prompt injection detected: '" + injectionPattern + "'");
            return failure(buildPromptInjectionResponse());
        }

        // 2. Check for malicious content
        String maliciousPattern = detectMaliciousContent(contentLower);
        if (maliciousPattern != null) {
            Log.warn("InputValidationGuardrail: BLOCKED - Malicious content detected: '" + maliciousPattern + "'");
            return failure(buildMaliciousContentResponse());
        }

        // 2.5. Check if this is a customer order question (allow these through)
        boolean isCustomerOrderQuestion = isCustomerOrderQuestion(content, contentLower);
        if (isCustomerOrderQuestion) {
            Log.info("InputValidationGuardrail: Allowing customer order question");
            // Skip off-topic checks for customer order questions, but still validate security
            return success();
        }

        // 3. Check for off-topic action requests (e.g., "book me a flight")
        String offTopicAction = detectOffTopicAction(contentLower);
        if (offTopicAction != null) {
            Log.warn("InputValidationGuardrail: BLOCKED - Off-topic action request: '" + offTopicAction + "'");
            return failure(buildOffTopicActionResponse(offTopicAction));
        }

        // 4. Check for completely off-topic questions
        String offTopicKeyword = detectCompletelyOffTopic(contentLower);
        if (offTopicKeyword != null) {
            Log.warn("InputValidationGuardrail: BLOCKED - Completely off-topic question: '" + offTopicKeyword + "'");
            return failure(buildCompletelyOffTopicResponse(offTopicKeyword));
        }

        // 5. Check for off-topic technology combinations
        String offTopicCombo = detectOffTopicCombination(contentLower);
        if (offTopicCombo != null) {
            Log.warn("InputValidationGuardrail: BLOCKED - Off-topic technology combination detected");
            return failure(offTopicCombo);
        }

        // 6. Check for non-CloudX products (unless in valid context)
        String nonCloudXProduct = detectNonCloudXProduct(contentLower);
        if (nonCloudXProduct != null && !isValidCloudXContext(contentLower)) {
            Log.warn("InputValidationGuardrail: BLOCKED - Non-CloudX product without valid context: '" + nonCloudXProduct + "'");
            return failure(buildNonCloudXProductResponse(nonCloudXProduct));
        }

        // Input is valid
        Log.info("InputValidationGuardrail: Input validated successfully");
        return success();
    }

    /**
     * Detects prompt injection attempts
     */
    private String detectPromptInjection(String content) {
        for (String pattern : PROMPT_INJECTION_PATTERNS) {
            if (content.contains(pattern)) {
                return pattern;
            }
        }
        return null;
    }

    /**
     * Detects malicious content patterns
     */
    private String detectMaliciousContent(String content) {
        for (String pattern : MALICIOUS_PATTERNS) {
            if (content.contains(pattern)) {
                return pattern;
            }
        }
        return null;
    }

    /**
     * Detects off-topic action requests (e.g., "book me a flight")
     */
    private String detectOffTopicAction(String content) {
        for (String action : OFF_TOPIC_ACTIONS) {
            if (content.contains(action)) {
                return action;
            }
        }
        return null;
    }

    /**
     * Detects completely off-topic questions
     */
    private String detectCompletelyOffTopic(String content) {
        for (String keyword : COMPLETELY_OFF_TOPIC) {
            if (content.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    /**
     * Detects off-topic technology combinations
     */
    private String detectOffTopicCombination(String content) {
        for (String[] combo : OFF_TOPIC_COMBINATIONS) {
            String tech = combo[0];
            String unsupportedContext = combo[1];
            String message = combo[2];
            
            if (content.contains(tech) && content.contains(unsupportedContext)) {
                return message;
            }
        }
        return null;
    }

    /**
     * Detects non-CloudX products
     */
    private String detectNonCloudXProduct(String content) {
        for (String product : NON_CLOUDX_PRODUCTS) {
            if (content.contains(product)) {
                return product;
            }
        }
        return null;
    }

    /**
     * Checks if non-CloudX product is mentioned in valid context
     * (comparison, migration, integration)
     */
    private boolean isValidCloudXContext(String content) {
        String[] validContextKeywords = {
            "cloudx", "compare", "comparison", "versus", "vs",
            "migrate", "migration", "move from", "switch from",
            "integrate", "integration", "alternative to",
            "replace", "instead of"
        };
        
        for (String keyword : validContextKeywords) {
            if (content.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects if the question is about a CloudX customer order
     * Examples: "What did my cloudx customer order with order ORD-123?"
     *           "What is the status of ORD-456?"
     *           "Tell me about my customer order ORD-123"
     */
    private boolean isCustomerOrderQuestion(String originalContent, String contentLower) {
        // Check for order ID pattern (ORD-XXX)
        boolean hasOrderId = ORDER_ID_PATTERN.matcher(originalContent).find();
        
        // Check for customer order keywords
        boolean hasOrderKeywords = false;
        for (String keyword : CUSTOMER_ORDER_KEYWORDS) {
            if (contentLower.contains(keyword)) {
                hasOrderKeywords = true;
                break;
            }
        }
        
        // If it has an order ID, it's likely a customer order question
        if (hasOrderId) {
            Log.debug("InputValidationGuardrail: Detected order ID pattern in question");
            return true;
        }
        
        // If it has order keywords AND CloudX context, it's a customer order question
        if (hasOrderKeywords && isValidCloudXContext(contentLower)) {
            Log.debug("InputValidationGuardrail: Detected customer order question with CloudX context");
            return true;
        }
        
        return false;
    }

    /**
     * Builds response for prompt injection attempts
     */
    private String buildPromptInjectionResponse() {
        return "I cannot process this request as it appears to contain instructions that would " +
               "compromise my intended function. I'm designed to assist with CloudX Enterprise Platform " +
               "sales enablement questions, including product features, pricing, competitive analysis, " +
               "and sales methodology. Please ask a question related to these topics.";
    }

    /**
     * Builds response for malicious content
     */
    private String buildMaliciousContentResponse() {
        return "I cannot process this request as it contains potentially malicious content. " +
               "I'm here to help with CloudX Enterprise Platform sales enablement questions. " +
               "Please ask about CloudX features, pricing, competitive positioning, or sales strategies.";
    }

    /**
     * Builds response for off-topic action requests
     */
    private String buildOffTopicActionResponse(String action) {
        return "I cannot assist with personal service requests like '" + action + "'. " +
               "I'm a CloudX Enterprise Platform sales enablement assistant. I can help you with:\n\n" +
               "• CloudX features, capabilities, and technical architecture\n" +
               "• Pricing, packaging, and ROI information\n" +
               "• Competitive analysis and positioning\n" +
               "• Sales methodology and processes\n" +
               "• Customer success stories and case studies\n" +
               "• Migration and implementation strategies\n" +
               "• CloudX customer order details (e.g., ORD-123)\n\n" +
               "Please ask a question related to CloudX sales enablement.";
    }

    /**
     * Builds response for completely off-topic questions
     */
    private String buildCompletelyOffTopicResponse(String keyword) {
        return "I specialize in CloudX Enterprise Platform sales enablement and cannot assist with " +
               "questions about " + keyword + ". I can help you with:\n\n" +
               "• CloudX features, capabilities, and technical architecture\n" +
               "• Pricing, packaging, and ROI information\n" +
               "• Competitive analysis and positioning\n" +
               "• Sales methodology and processes\n" +
               "• Customer success stories and case studies\n" +
               "• Migration and implementation strategies\n" +
               "• CloudX customer order details (e.g., ORD-123)\n\n" +
               "Please ask a question related to CloudX sales enablement.";
    }

    /**
     * Builds response for non-CloudX products without valid context
     */
    private String buildNonCloudXProductResponse(String product) {
        return "I specialize in CloudX Enterprise Platform sales enablement. " +
               "While I can discuss " + product + " in the context of CloudX comparisons, migrations, " +
               "or integrations, I cannot provide standalone information about it. " +
               "If you're interested in how CloudX compares to or integrates with " + product + ", " +
               "please rephrase your question to include CloudX in the context.";
    }
}