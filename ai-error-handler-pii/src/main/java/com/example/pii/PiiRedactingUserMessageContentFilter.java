package com.example.pii;

import org.jboss.logging.Logger;

import dev.langchain4j.data.message.AiMessage;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrail;
import io.quarkiverse.langchain4j.guardrails.OutputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PiiRedactingUserMessageContentFilter implements OutputGuardrail {

    @Inject
    PiiRedactionEngine piiRedactionEngine;

    private static final Logger LOG = Logger.getLogger(PiiRedactingUserMessageContentFilter.class);

    @Override
    public OutputGuardrailResult validate(AiMessage responseFromLLM) {
        String content = responseFromLLM.text();
        String redactedContent = piiRedactionEngine.redact(content);
        if (!content.equals(redactedContent)) {
            LOG.debugf(content, redactedContent);
            return reprompt(
                    "Response contains PII.", "Make sure to remove " + redactedContent);
        }
        return success();
    }
}