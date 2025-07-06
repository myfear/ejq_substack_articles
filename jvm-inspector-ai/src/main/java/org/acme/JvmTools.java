package org.acme;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.stream.Collectors;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JvmTools {

    record JvmProcess(String processId, String displayName) {
    }

    @Tool("Displays all the JVM processes available on the current host")
    public List<JvmProcess> getJvmProcesses() {
        return ProcessHandle.allProcesses()
                .filter(p -> p.info().command().map(cmd -> cmd.contains("java")).orElse(false))
                .map(p -> new JvmProcess(String.valueOf(p.pid()),
                        p.info().commandLine().orElse("unknown")))
                .collect(Collectors.toList());
    }

    @Tool("Performs a thread dump on a specific JVM process")
    public String threadDump(String processId) {
        long currentPid = ProcessHandle.current().pid();
        if (!String.valueOf(currentPid).equals(processId)) {
            return "Only the current process can be inspected in this example.";
        }

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        StringBuilder dump = new StringBuilder();
        for (ThreadInfo ti : bean.dumpAllThreads(true, true)) {
            dump.append(ti.toString());
        }
        return dump.toString();
    }

    @Tool("Analyzes thread states to identify deadlocks, blocked threads, and long-running tasks")
    public String analyzeThreads() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        StringBuilder analysis = new StringBuilder();
        
        // Check for deadlocks
        long[] deadlockedThreads = bean.findDeadlockedThreads();
        if (deadlockedThreads != null) {
            analysis.append("DEADLOCK DETECTED!\n");
            analysis.append("Deadlocked threads: ").append(deadlockedThreads.length).append("\n");
            for (long threadId : deadlockedThreads) {
                ThreadInfo info = bean.getThreadInfo(threadId);
                if (info != null) {
                    analysis.append("  - Thread '").append(info.getThreadName())
                            .append("' (ID: ").append(threadId).append(")")
                            .append(" is ").append(info.getThreadState())
                            .append(" waiting on: ").append(info.getLockName()).append("\n");
                }
            }
            analysis.append("\n");
        }
        
        // Analyze all threads
        ThreadInfo[] allThreads = bean.dumpAllThreads(false, false);
        int blockedCount = 0;
        int waitingCount = 0;
        int timedWaitingCount = 0;
        
        analysis.append("THREAD ANALYSIS:\n");
        analysis.append("Total threads: ").append(allThreads.length).append("\n\n");
        
        for (ThreadInfo thread : allThreads) {
            Thread.State state = thread.getThreadState();
            switch (state) {
                case BLOCKED:
                    blockedCount++;
                    analysis.append("BLOCKED: '").append(thread.getThreadName())
                            .append("' waiting for lock: ").append(thread.getLockName()).append("\n");
                    break;
                case WAITING:
                    waitingCount++;
                    if (thread.getThreadName().contains("sleeping") || thread.getThreadName().contains("long")) {
                        analysis.append("WAITING: '").append(thread.getThreadName())
                                .append("' - likely long-running task\n");
                    }
                    break;
                case TIMED_WAITING:
                    timedWaitingCount++;
                    if (thread.getThreadName().contains("sleeping") || thread.getThreadName().contains("long")) {
                        analysis.append("TIMED_WAITING: '").append(thread.getThreadName())
                                .append("' - likely long-running task\n");
                    }
                    break;
            }
        }
        
        analysis.append("\nSUMMARY:\n");
        analysis.append("- Blocked threads: ").append(blockedCount).append("\n");
        analysis.append("- Waiting threads: ").append(waitingCount).append("\n");
        analysis.append("- Timed waiting threads: ").append(timedWaitingCount).append("\n");
        
        if (deadlockedThreads == null && blockedCount == 0) {
            analysis.append("No deadlocks or blocked threads detected.\n");
        }
        
        return analysis.toString();
    }
}