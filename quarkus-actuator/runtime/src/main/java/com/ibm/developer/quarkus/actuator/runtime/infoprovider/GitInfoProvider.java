package com.ibm.developer.quarkus.actuator.runtime.infoprovider;

import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GitInfoProvider extends AbstractInfoProvider {

    public Map<String, Object> getGitInfo() {
        Map<String, Object> git = newMap();

        // Read git info from config (loaded at build time)
        var gitConfig = config.info().git();
        String branch = gitConfig.branch().orElse(null);
        String commitId = gitConfig.commitId().orElse(null);
        String commitTime = gitConfig.commitTime().orElse(null);

        log.infof("GitInfoProvider - branch: %s, commitId: %s, commitTime: %s", branch, commitId, commitTime);

        // Only add branch if it exists
        if (branch != null && !branch.isEmpty()) {
            git.put("branch", branch);
        }

        // Only add commit if at least one commit property exists (matching Spring Boot format)
        if ((commitId != null && !commitId.isEmpty()) || (commitTime != null && !commitTime.isEmpty())) {
            Map<String, Object> commit = newMap();
            if (commitId != null && !commitId.isEmpty()) {
                commit.put("id", commitId);
            }
            if (commitTime != null && !commitTime.isEmpty()) {
                commit.put("time", commitTime);
            }
            if (!commit.isEmpty()) {
                git.put("commit", commit);
            }
        }

        if (git.isEmpty()) {
            log.infof("No git info found in config - all values were empty or null. branch=%s, commitId=%s, commitTime=%s", branch, commitId, commitTime);
        } else {
            log.infof("Git info from config: branch=%s, commit.id=%s",
                    git.get("branch"),
                    git.containsKey("commit") ? ((Map<?, ?>) git.get("commit")).get("id") : "N/A");
        }

        return git;
    }
}
