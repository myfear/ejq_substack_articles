package org.example.steps;


import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.PromptTemplate;
import io.quarkus.logging.Log;

import org.example.ReasoningStep;
import org.example.model.StepResult;
import org.example.util.TextUtils;

import java.util.Map;

public class ProblemAnalysisStep extends ReasoningStep {

    public ProblemAnalysisStep() {
        super("Problem Analysis",
                "You are an expert problem analyzer. Break down the given problem into key components, identify what information is provided, what needs to be found, and any constraints or assumptions.");
    }

    @Override
    public StepResult execute(String input, Map<String, Object> context, ChatModel model) {
        PromptTemplate template = PromptTemplate.from("""
                    Carefully analyze this problem step by step:
                    
                    Problem: {{problem}}
                    
                    Think through it systematically:
                    
                    First, identify what information is explicitly given in the problem.
                    Then, determine exactly what we need to find or solve for.
                    Next, consider any constraints, limitations, or assumptions that apply.
                    Finally, classify what type of problem this is to guide our solution approach.
                    
                    Step-by-step analysis:
                    
                    **Step 1 - Given Information:**
                    [What facts, numbers, or conditions are provided?]
                    
                    **Step 2 - What We Need to Find:**
                    [What is the question asking for? What is the unknown?]
                    
                    **Step 3 - Constraints and Assumptions:**
                    [Are there any limitations, rules, or assumptions we need to consider?]
                    
                    **Step 4 - Problem Classification:**
                    [What type of problem is this? (arithmetic, algebraic, geometric, word problem, etc.)]
                    
                    **Step 5 - Verification of Understanding:**
                    [Do you understand the problem correctly? Is anything unclear?]
                """);

        String rawResponse = model.chat(UserMessage.from(template.apply(Map.of(
                "problem", input)).text())).aiMessage().text();
        String cleanResponse = TextUtils.cleanText(rawResponse);
        context.put("problem_analysis", cleanResponse);
        Log.infof("Problem Analysis Step: %s", cleanResponse);
        return new StepResult(name, input, cleanResponse, "Analyzed problem components and identified key elements", true);
    }
}
