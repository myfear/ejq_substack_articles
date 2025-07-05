package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.model.ChainOfThoughtResult;
import org.example.model.StepResult;

import dev.langchain4j.model.chat.ChatModel;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChainOfThoughtPipeline {
    private final ChatModel chatModel;
    private final List<ReasoningStep> steps = new ArrayList<>();
    private final Map<String, Object> context = new HashMap<>();
    private final List<StepResult> executionHistory = new ArrayList<>();

    public ChainOfThoughtPipeline(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public ChainOfThoughtPipeline addStep(ReasoningStep step) {
        steps.add(step);
        return this;
    }

    public ChainOfThoughtResult execute(String input) {
        String currentInput = input;
        context.put("original_input", input);

        Log.infof("Analysis Steps: %s", steps.size());


        for (ReasoningStep step : steps) {
            try {
                StepResult result = step.execute(currentInput, context, chatModel);
                executionHistory.add(result);
                if (!result.isSuccess()) break;
                currentInput = result.getOutput();
            } catch (Exception e) {
                executionHistory.add(new StepResult(
                    step.getName(), currentInput, "", e.getMessage(), false
                ));
                break;
            }
        }

        boolean success = executionHistory.stream().allMatch(StepResult::isSuccess);
        return new ChainOfThoughtResult(success, currentInput, executionHistory);
    }
}

