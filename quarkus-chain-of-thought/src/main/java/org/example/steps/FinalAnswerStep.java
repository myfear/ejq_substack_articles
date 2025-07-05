package org.example.steps;

import java.util.Map;

import org.example.ReasoningStep;
import org.example.model.StepResult;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import io.quarkus.logging.Log;

import org.example.util.TextUtils;

public class FinalAnswerStep extends ReasoningStep {
    public FinalAnswerStep() {
        super("Final Answer", "Extract the clean, final answer from the verified solution.");
    }

    @Override
    public StepResult execute(String input, Map<String, Object> context, ChatModel model) {
        String verification = (String) context.get("solution_verification");
        String originalProblem = (String) context.get("original_input");

        PromptTemplate template = PromptTemplate.from("""
            Based on the verification results, you need to provide the clear, final answer.
            
            Original Problem: {{problem}}
            
            Verification Results: {{verification}}
            
            Now  extract and present the final answer clearly and concisely.
            
            **Task:** Provide only the direct answer to the original question.
            
            **Requirements:**
            - Be concise and specific
            - Include units if applicable
            - State the answer clearly without extra explanation
            - If the verification found errors, provide the corrected answer
            - This should be exactly what the user is looking for as their final result
            
            **Final Answer:**
            [Provide the clean, direct answer here]
        """);

        String rawResponse = model.chat(UserMessage.from(template.apply(Map.of(
            "problem", originalProblem,
            "verification", verification
        )).text())).aiMessage().text();
        
        String cleanResponse = TextUtils.cleanText(rawResponse);
        Log.infof("Final Answer Step: {}", cleanResponse);
        return new StepResult(name, input, cleanResponse, "Extracted the final answer", true);
    }
} 