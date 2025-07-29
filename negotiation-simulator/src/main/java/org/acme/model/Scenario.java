package org.acme.model;

public enum Scenario {
    REQUESTING_RAISE("The user is your employee who has requested this meeting to discuss a salary increase. They will present arguments based on their performance, achievements, market research, or other factors. Listen to their case, ask clarifying questions, and respond according to your personality type. You have the authority to approve, deny, or negotiate the request."),
    POOR_PERFORMANCE("You are conducting a performance review meeting because the user (your employee) has been experiencing performance issues that need to be addressed. You should discuss specific concerns about their work quality, productivity, missed deadlines, or other performance problems. Be constructive but clear about the issues and work toward solutions and expectations for improvement.");

    private final String prompt;

    Scenario(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }
}
