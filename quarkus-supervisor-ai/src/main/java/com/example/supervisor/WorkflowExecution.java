package com.example.supervisor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.messaging.AgentResult;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniEmitter;

public class WorkflowExecution {
    private final String workflowId;
    private final int expectedTaskCount;
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final List<AgentResult> results = new ArrayList<>();
    private final Map<String, Boolean> taskTracker = new ConcurrentHashMap<>();
    private UniEmitter<? super List<AgentResult>> completionEmitter;

    public WorkflowExecution(String workflowId, int expectedTaskCount) {
        this.workflowId = workflowId;
        this.expectedTaskCount = expectedTaskCount;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public boolean hasTask(String taskId) {
        return taskTracker.containsKey(taskId);
    }

    public void trackTask(String taskId) {
        taskTracker.put(taskId, false);
    }

    public synchronized void addResult(AgentResult result) {
        if (taskTracker.containsKey(result.taskId) && !taskTracker.get(result.taskId)) {
            taskTracker.put(result.taskId, true);
            results.add(result);
            int completed = completedTasks.incrementAndGet();

            if (completed >= expectedTaskCount && completionEmitter != null) {
                completionEmitter.complete(new ArrayList<>(results));
            }
        }
    }

    public Uni<List<AgentResult>> getCompletionUni() {
        return Uni.createFrom().emitter(emitter -> {
            this.completionEmitter = emitter;
            // Check if already completed
            if (completedTasks.get() >= expectedTaskCount) {
                emitter.complete(new ArrayList<>(results));
            }
        });
    }
}