package org.example.model;

public class StepResult {
    private final String stepName;
    private final String input;
    private final String output;
    private final String reasoning;
    private final boolean success;

    public StepResult(String stepName, String input, String output, String reasoning, boolean success) {
        this.stepName = stepName;
        this.input = input;
        this.output = output;
        this.reasoning = reasoning;
        this.success = success;
    }

    // Getters
    public String getStepName() { return stepName; }
    public String getInput() { return input; }
    public String getOutput() { return output; }
    public String getReasoning() { return reasoning; }
    public boolean isSuccess() { return success; }
}
