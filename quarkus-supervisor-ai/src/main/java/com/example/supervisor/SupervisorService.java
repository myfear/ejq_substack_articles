package com.example.supervisor;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import com.example.messaging.AgentResult;
import com.example.messaging.AgentTask;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SupervisorService {
    private static final Logger LOG = Logger.getLogger(SupervisorService.class);

    @Inject
    @Channel("agent-tasks-out")
    Emitter<AgentTask> taskEmitter;

    // In-memory store for tracking workflows. Key is workflowId.
    private final Map<String, WorkflowExecution> activeWorkflows = new ConcurrentHashMap<>();

    // This is a simplified workflow request.
    public record WorkflowRequest(List<AgentTask> tasks) {
    }

    // Main method to start a workflow
    public Uni<List<AgentResult>> executeWorkflow(WorkflowRequest request) {
        String workflowId = UUID.randomUUID().toString();
        LOG.infof("Starting workflow %s with %d tasks", workflowId, request.tasks().size());

        WorkflowExecution execution = new WorkflowExecution(workflowId, request.tasks().size());
        activeWorkflows.put(workflowId, execution);

        // Dispatch all tasks to Kafka
        request.tasks().forEach(task -> {
            // Add workflowId to task metadata for tracking
            task.metadata.put("workflowId", workflowId);
            // Track this task in the execution
            execution.trackTask(task.taskId);
            // Send the task directly
            taskEmitter.send(task);
            LOG.infof("Dispatched task %s for agent %s", task.taskId, task.agentType);
        });

        // Return a Uni that will complete when results are ready or time out
        return execution.getCompletionUni()
                .ifNoItem().after(Duration.ofSeconds(60)).fail() // Timeout
                .eventually(() -> activeWorkflows.remove(workflowId)); // Cleanup
    }

    // Kafka consumer for results
    @Incoming("agent-results-in")
    public void handleAgentResult(AgentResult result) {
        if (result.taskId == null)
            return; // Ignore invalid messages

        // Find the workflow this result belongs to
        activeWorkflows.values().stream()
                .filter(exec -> exec.hasTask(result.taskId))
                .findFirst()
                .ifPresent(execution -> {
                    LOG.infof("Received result for task %s in workflow %s", result.taskId, execution.getWorkflowId());
                    execution.addResult(result);
                });
    }
}