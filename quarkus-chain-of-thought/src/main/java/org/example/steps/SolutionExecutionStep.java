package org.example.steps;

import java.util.Map;

import org.example.ReasoningStep;
import org.example.model.StepResult;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import io.quarkus.logging.Log;

import org.example.util.TextUtils;

public class SolutionExecutionStep extends ReasoningStep {
    public SolutionExecutionStep() {
        super("Solution Execution", "Perform calculations and solve.");
    }

    @Override
    public StepResult execute(String input, Map<String, Object> context, ChatModel model) {
        String plan = (String) context.get("solution_plan");
        String originalProblem = (String) context.get("original_input");

        PromptTemplate template = PromptTemplate.from("""
            Execute the solution plan step by step:
            
            Original Problem: {{problem}}
            
            Solution Plan: {{plan}}
            
            Work through this carefully, showing each step and checking the work as you go:
            
            **Execution Process:**
            
            Follow the plan exactly, performing each calculation or operation step by step.
            For each step, show:
            - What youre doing and why
            - The actual calculation or operation
            - The intermediate result
            - A quick check to ensure it makes sense
            
            **Step-by-Step Execution:**
            
            [For each planned step, show:]
            **Step X:** [Description of what you're doing]
            - Calculation: [Show the actual math/work]
            - Result: [The intermediate answer]
            - Check: [Quick verification this step makes sense]
            
            **Final Answer:**
            [Clear statement of the final result]
        """);

        String rawResponse = model.chat(UserMessage.from(template.apply(Map.of(
            "problem", originalProblem,
            "plan", plan
        )).text())).aiMessage().text();

        String cleanResponse = TextUtils.cleanText(rawResponse);
        context.put("solution_execution", cleanResponse);
        Log.infof("Solution Execution Step: {}", cleanResponse);
        return new StepResult(name, input, cleanResponse, "Executed the plan", true);
    }
}

