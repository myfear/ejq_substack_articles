package com.example.messaging;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AgentTask {
    public String taskId;
    public String agentType;
    public String content;
    public int priority;
    public Map<String, Object> metadata = new HashMap<>();

    public AgentTask() {
    }

    public AgentTask(String agentType, String content, int priority) {
        this.taskId = UUID.randomUUID().toString();
        this.agentType = agentType;
        this.content = content;
        this.priority = priority;
    }
}