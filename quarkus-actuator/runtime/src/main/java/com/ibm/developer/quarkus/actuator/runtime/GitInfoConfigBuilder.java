package com.ibm.developer.quarkus.actuator.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.logging.Logger;

import io.quarkus.runtime.configuration.ConfigBuilder;
import io.smallrye.config.SmallRyeConfigBuilder;

public class GitInfoConfigBuilder implements ConfigBuilder {

    private static final Logger log = Logger.getLogger(GitInfoConfigBuilder.class);

    @Override
    public SmallRyeConfigBuilder configBuilder(SmallRyeConfigBuilder builder) {
        // Read git.properties at build time (this is called during static init)
        String location = "/git.properties";
        Properties props = new Properties();

        try (InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(location)) {
            if (is == null) {
                // Try without leading slash
                String locationWithoutSlash = location.substring(1);
                try (InputStream is2 = Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream(locationWithoutSlash)) {
                    if (is2 != null) {
                        props.load(is2);
                        log.debugf("Loaded git.properties from: %s", locationWithoutSlash);
                    } else {
                        log.debugf("git.properties not found at: %s or %s", location, locationWithoutSlash);
                    }
                }
            } else {
                props.load(is);
                log.debugf("Loaded git.properties from: %s", location);
            }
        } catch (IOException e) {
            log.warnf("Failed to load git.properties: %s", e.getMessage());
        }

        // Extract the relevant git properties and add them to config via ConfigSource
        Map<String, String> gitProperties = new HashMap<>();
        if (!props.isEmpty()) {
            String branch = props.getProperty("git.branch");
            String commitId = props.getProperty("git.commit.id.abbrev");
            if (commitId == null || commitId.isEmpty()) {
                commitId = props.getProperty("git.commit.id");
            }
            String commitTime = props.getProperty("git.commit.time");

            if (branch != null && !branch.isEmpty()) {
                gitProperties.put("actuator.info.git.branch", branch);
            }
            if (commitId != null && !commitId.isEmpty()) {
                gitProperties.put("actuator.info.git.commit-id", commitId);
            }
            if (commitTime != null && !commitTime.isEmpty()) {
                gitProperties.put("actuator.info.git.commit-time", commitTime);
            }

            log.infof("Git info loaded at build time - branch: %s, commit: %s, time: %s",
                    branch != null ? branch : "N/A",
                    commitId != null ? commitId : "N/A",
                    commitTime != null ? commitTime : "N/A");
        } else {
            log.debug("No git properties found, git info will be empty");
        }

        // Add a ConfigSource with the git properties
        // Note: We use withSources to add the ConfigSource, which makes the properties
        // available to the config system. The ConfigSource will be queried when
        // the config values are accessed.
        if (!gitProperties.isEmpty()) {
            GitInfoConfigSource configSource = new GitInfoConfigSource(gitProperties);
            builder.withSources(configSource);
            log.infof("Added GitInfoConfigSource with %d properties: %s", 
                    gitProperties.size(), gitProperties.keySet());
        } else {
            log.debug("No git properties to add to ConfigSource");
        }

        return builder;
    }

    private static class GitInfoConfigSource implements ConfigSource {
        private final Map<String, String> properties;

        GitInfoConfigSource(Map<String, String> properties) {
            this.properties = properties;
        }

        @Override
        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public Set<String> getPropertyNames() {
            return properties.keySet();
        }

        @Override
        public String getValue(String propertyName) {
            String value = properties.get(propertyName);
            if (value != null) {
                log.debugf("GitInfoConfigSource.getValue(%s) = %s", propertyName, value);
            } else {
                log.debugf("GitInfoConfigSource.getValue(%s) = null (not found)", propertyName);
            }
            return value;
        }

        @Override
        public String getName() {
            return "GitInfoConfigSource";
        }

        @Override
        public int getOrdinal() {
            // Use a high ordinal to ensure this config source takes precedence
            return 250;
        }
    }
}

