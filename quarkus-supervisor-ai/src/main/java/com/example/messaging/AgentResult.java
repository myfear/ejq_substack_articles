package com.example.messaging;

public class AgentResult {
    public String taskId;
    public String agentId;
    public String agentType;
    public String result;
    public String status;
    public long executionTime;
    public String error;

    public AgentResult() {
    }

    public AgentResult(String taskId, String agentId, String agentType, String result, String status) {
        this.taskId = taskId;
        this.agentId = agentId;
        this.agentType = agentType;
        this.result = result;
        this.status = status;
    }
}
