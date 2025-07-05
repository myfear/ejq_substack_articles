package org.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the final outcome of a Chain of Thought execution.
 * Contains the final answer, a success flag, and the full execution history.
 */
public class ChainOfThoughtResult {
    private final boolean success;
    private final String finalResult;
    private final List<StepResult> executionHistory;

    public ChainOfThoughtResult(boolean success, String finalResult, List<StepResult> executionHistory) {
        this.success = success;
        this.finalResult = finalResult;
        this.executionHistory = new ArrayList<>(executionHistory); // defensive copy
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFinalResult() {
        return finalResult;
    }

    public List<StepResult> getExecutionHistory() {
        return executionHistory;
    }
}
