package com.ibm.developer.quarkus.actuator.runtime.infoprovider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GitInfoProvider extends AbstractInfoProvider {

    private volatile Map<String, Object> cached;

    public Map<String, Object> getGitInfo() {
        if (cached == null) {
            synchronized (this) {
                if (cached == null) {
                    cached = load();
                }
            }
        }
        return cached;
    }

    private Map<String, Object> load() {
        Properties props = new Properties();
        String location = config.info().gitPropertiesLocation();
        log.debugf("Loading git properties from: %s", location);

        // Try multiple ways to load the resource
        InputStream is = null;

        // First try with the class loader (for resources in the application)
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(location);
        if (is == null) {
            // Try with leading slash removed
            String locationWithoutSlash = location.startsWith("/") ? location.substring(1) : location;
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(locationWithoutSlash);
            if (is != null) {
                log.debugf("Found git.properties using path without leading slash: %s", locationWithoutSlash);
            }
        } else {
            log.debugf("Found git.properties using context class loader: %s", location);
        }

        if (is == null) {
            // Try with this class's class loader
            is = getClass().getClassLoader().getResourceAsStream(location);
            if (is != null) {
                log.debugf("Found git.properties using class loader: %s", location);
            }
        }

        if (is == null) {
            // Try with getResourceAsStream (relative to this class)
            is = getClass().getResourceAsStream(location);
            if (is != null) {
                log.debugf("Found git.properties using getResourceAsStream: %s", location);
            }
        }

        if (is != null) {
            try (InputStream stream = is) {
                props.load(stream);
                log.debugf("Successfully loaded %d git properties from %s", props.size(), location);
                if (log.isDebugEnabled()) {
                    props.stringPropertyNames()
                            .forEach(key -> log.debugf("  Found property: %s = %s", key, props.getProperty(key)));
                }
            } catch (IOException e) {
                log.warnf("Failed to load git properties from %s: %s", location, e.getMessage());
            }
        } else {
            log.warnf("git.properties file not found at: %s", location);
        }

        Map<String, Object> git = newMap();
        if (props.isEmpty()) {
            log.debug("No git properties found, returning empty map");
            return git;
        }

        String branch = props.getProperty("git.branch");
        String commitId = props.getProperty("git.commit.id.abbrev");
        if (commitId == null || commitId.isEmpty()) {
            commitId = props.getProperty("git.commit.id");
        }
        String commitTime = props.getProperty("git.commit.time");

        // Only add branch if it exists
        if (branch != null && !branch.isEmpty()) {
            git.put("branch", branch);
        }

        // Only add commit if at least one commit property exists (matching Spring Boot
        // format)
        if (commitId != null || commitTime != null) {
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
            log.debugf("Git properties file loaded but no expected properties found. Available keys: %s",
                    props.stringPropertyNames());
        } else {
            log.debugf("Git info loaded: branch=%s, commit.id=%s",
                    git.get("branch"),
                    git.containsKey("commit") ? ((Map<?, ?>) git.get("commit")).get("id") : "N/A");
        }
        return git;
    }
}
