package com.example.agents.impl;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.example.agents.ResearchAgent;
import com.example.messaging.AgentResult;
import com.example.messaging.AgentTask;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ResearchAgentWorker extends BaseAgentWorker<ResearchAgent> {

    @Inject
    ResearchAgent agent;

    public ResearchAgentWorker() {
        super(null); // Will be set via field injection
    }

    @Override
    protected String getAgentType() {
        return agent.getAgentType();
    }

    @Override
    protected String processTaskWithAgent(String content) {
        return agent.processTask(content);
    }

    @Incoming("agent-tasks-in")
    @Outgoing("agent-results-out")
    @Override
    public Message<AgentResult> process(Message<AgentTask> message) {
        return super.process(message);
    }
}