package org.example.steps;

import java.util.Map;

import org.example.ReasoningStep;
import org.example.model.StepResult;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import io.quarkus.logging.Log;

import org.example.util.TextUtils;

public class VerificationStep extends ReasoningStep {
    public VerificationStep() {
        super("Solution Verification", "Verify the solution and provide the final answer.");
    }

    @Override
    public StepResult execute(String input, Map<String, Object> context, ChatModel model) {
        String solution = (String) context.get("solution_execution");
        String originalProblem = (String) context.get("original_input");

        PromptTemplate template = PromptTemplate.from("""
            Verify this solution and provide the final, clean answer.
            
            Original Problem: {{problem}}
            
            Proposed Solution: {{solution}}
            
            First, let me verify the solution is correct:
            
            **Verification Process:**
            1. Check if calculations are mathematically correct
            2. Ensure the solution answers the original question
            3. Verify the answer is reasonable and makes sense
            4. Confirm units and format are appropriate
            
            **Verification Results:**
            [Brief verification - is the solution correct? Any errors found?]
            
            **Final Answer:**
            Based on my verification, here is the clean, final answer to the problem:
            
            [Provide only the direct, clear answer to the original question. Be concise and specific. Include units if applicable. This should be the exact answer the user is looking for, not the verification process.]
            
            If the solution had errors, provide the corrected answer. If the solution was correct, restate the answer clearly and concisely.
        """);

        String rawResponse = model.chat(UserMessage.from(template.apply(Map.of(
            "problem", originalProblem,
            "solution", solution
        )).text())).aiMessage().text();
        String cleanResponse = TextUtils.cleanText(rawResponse);
        context.put("solution_verification", cleanResponse);
        Log.infof("Solution Verification Step: {}", cleanResponse);
        return new StepResult(name, input, cleanResponse, "Verified solution and extracted final answer", true);
    }
}
