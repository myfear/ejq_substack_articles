package org.acme.spy;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgentRegistry {

    private final Map<String, Agent> agents = new ConcurrentHashMap<>();

    public Agent enroll(Agent agent) {
        agents.put(agent.getCodeName(), agent);
        return agent;
    }

    public Agent find(String codeName) {
        return agents.get(codeName);
    }

    public Collection<Agent> all() {
        return agents.values();
    }
}