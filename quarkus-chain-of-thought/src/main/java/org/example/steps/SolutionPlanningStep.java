package org.example.steps;

import java.util.Map;

import org.example.ReasoningStep;
import org.example.model.StepResult;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import io.quarkus.logging.Log;

import org.example.util.TextUtils;

public class SolutionPlanningStep extends ReasoningStep {
    public SolutionPlanningStep() {
        super("Solution Planning", "Generate a plan to solve the problem.");
    }

    @Override
    public StepResult execute(String input, Map<String, Object> context, ChatModel model) {
        String analysis = (String) context.get("problem_analysis");

        PromptTemplate template = PromptTemplate.from("""
            Create a detailed solution plan based on the analysis.
            
            Problem Analysis: {{analysis}}
            
            Think step by step about the best approach to solve this problem:
            
            First, I'll consider what method or strategy would be most effective.
            Then, I'll break down the solution into clear, logical steps.
            Next, I'll think about the order of operations and any dependencies between steps.
            Finally, I'll review my plan to ensure it's complete and logical.
            
            My step-by-step solution planning:
            
            **Step 1 - Choose Solution Strategy:**
            [What approach or method should I use? Why is this the best choice?]
            
            **Step 2 - Break Down the Solution:**
            [What are the individual steps needed to solve this problem?]
            
            **Step 3 - Order and Dependencies:**
            [In what order should I perform these steps? Do any steps depend on others?]
            
            **Step 4 - Required Calculations or Operations:**
            [What specific calculations, formulas, or operations will I need?]
            
            **Step 5 - Verification Plan:**
            [How can I check that each step is correct as I go?]
            
            **Final Solution Plan:**
            [Numbered list of the exact steps to execute]
        """);

        String rawResponse = model.chat(UserMessage.from(template.apply(Map.of("analysis", analysis)).text())).aiMessage().text();
        String cleanResponse = TextUtils.cleanText(rawResponse);
        context.put("solution_plan", cleanResponse);
        Log.infof("Solution Planning Step: {}", cleanResponse);
        return new StepResult(name, input, cleanResponse, "Created a solution plan", true);
    }
}

