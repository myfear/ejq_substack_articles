package com.example.agents.impl;

import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Message;

import com.example.messaging.AgentResult;
import com.example.messaging.AgentTask;

import io.quarkus.logging.Log;

public abstract class BaseAgentWorker<T> {

    protected final String agentId;
    protected T agent;

    public BaseAgentWorker(T agent) {
        this.agentId = UUID.randomUUID().toString();
        this.agent = agent;
    }

    // Abstract methods to be implemented by subclasses
    protected abstract String getAgentType();

    protected abstract String processTaskWithAgent(String content);

    // Method to be overridden in concrete classes with messaging annotations
    protected Message<AgentResult> process(Message<AgentTask> message) {
        AgentTask task = message.getPayload();

        // Only process tasks for this agent type
        if (!getAgentType().equals(task.agentType)) {
            message.ack(); // Acknowledge the message to avoid reprocessing
            return null; // Don't forward to the outgoing channel
        }

        Log.infof("Agent %s (%s) processing task %s", getAgentType(), agentId, task.taskId);
        long startTime = System.currentTimeMillis();

        try {
            String result = processTaskWithAgent(task.content);
            long executionTime = System.currentTimeMillis() - startTime;

            AgentResult agentResult = new AgentResult(task.taskId, agentId, getAgentType(), result, "COMPLETED");
            agentResult.executionTime = executionTime;

            Log.infof("Agent %s completed task %s in %dms", getAgentType(), task.taskId, executionTime);
            return Message.of(agentResult).withAck(message::ack);
        } catch (Exception e) {
            Log.errorf(e, "Agent %s failed to process task %s", getAgentType(), task.taskId);
            AgentResult errorResult = new AgentResult(task.taskId, agentId, getAgentType(), null, "FAILED");
            errorResult.error = e.getMessage();
            return Message.of(errorResult).withAck(message::ack);
        }
    }
}
