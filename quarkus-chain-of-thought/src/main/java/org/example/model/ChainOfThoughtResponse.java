package org.example.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response model for the Chain of Thought API endpoint.
 * Provides a structured view of the problem-solving process.
 */
public class ChainOfThoughtResponse {
    private final String problem;
    private final boolean success;
    private final String finalResult;
    private final List<StepResponse> steps;
    private final LocalDateTime timestamp;

    public ChainOfThoughtResponse(String problem, ChainOfThoughtResult result) {
        this.problem = problem;
        this.success = result.isSuccess();
        this.finalResult = result.getFinalResult();
        this.steps = result.getExecutionHistory().stream()
                .map(StepResponse::new)
                .collect(Collectors.toList());
        this.timestamp = LocalDateTime.now();
    }

    public String getProblem() {
        return problem;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public List<StepResponse> getSteps() {
        return steps;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public static class StepResponse {
        private final String stepName;
        private final String input;
        private final String output;
        private final String reasoning;
        private final boolean success;

        public StepResponse(StepResult stepResult) {
            this.stepName = stepResult.getStepName();
            this.input = stepResult.getInput();
            this.output = stepResult.getOutput();
            this.reasoning = stepResult.getReasoning();
            this.success = stepResult.isSuccess();
        }

        public String getStepName() {
            return stepName;
        }

        public String getInput() {
            return input;
        }

        public String getOutput() {
            return output;
        }

        public String getReasoning() {
            return reasoning;
        }

        public boolean isSuccess() {
            return success;
        }
    }
} 