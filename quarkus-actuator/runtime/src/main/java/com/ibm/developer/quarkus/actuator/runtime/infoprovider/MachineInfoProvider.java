package com.ibm.developer.quarkus.actuator.runtime.infoprovider;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MachineInfoProvider extends AbstractInfoProvider {

    public Map<String, Object> getOsInfo() {
        Map<String, Object> os = newMap();
        os.put("name", System.getProperty("os.name"));
        os.put("version", System.getProperty("os.version"));
        os.put("arch", System.getProperty("os.arch"));
        return os;
    }

    public Map<String, Object> getProcessInfo() {
        Map<String, Object> process = newMap();
        
        // PID
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        String processName = runtimeBean.getName();
        long pid = -1;
        try {
            if (processName.contains("@")) {
                pid = Long.parseLong(processName.split("@")[0]);
            }
        } catch (NumberFormatException e) {
            // Ignore
        }
        
        // Try ProcessHandle API (Java 9+)
        try {
            ProcessHandle currentProcess = ProcessHandle.current();
            pid = currentProcess.pid();
        } catch (Exception e) {
            // Fall back to RuntimeMXBean approach
        }
        
        if (pid > 0) {
            process.put("pid", pid);
        }
        
        // Parent PID using ProcessHandle API (Java 9+)
        try {
            ProcessHandle currentProcess = ProcessHandle.current();
            ProcessHandle parentProcess = currentProcess.parent().orElse(null);
            if (parentProcess != null) {
                process.put("parentPid", parentProcess.pid());
            }
        } catch (Exception e) {
            // Fall back to system property if ProcessHandle not available
            String parentPid = System.getProperty("jvm.parent.pid");
            if (parentPid != null) {
                try {
                    process.put("parentPid", Long.parseLong(parentPid));
                } catch (NumberFormatException ex) {
                    // Ignore
                }
            }
        }
        
        // Owner
        String userName = System.getProperty("user.name");
        if (userName != null) {
            process.put("owner", userName);
        }
        
        // CPUs
        process.put("cpus", Runtime.getRuntime().availableProcessors());
        
        return process;
    }
}

