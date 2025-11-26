package com.ibm.developer.quarkus.actuator.runtime;

import com.ibm.developer.quarkus.actuator.runtime.infoprovider.BuildInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.GitInfoProvider;
import io.quarkus.runtime.annotations.Recorder;

import java.util.Map;
import java.util.function.Supplier;

@Recorder
public class InfoRecorder {

    public Supplier<GitInfoProvider> gitInfoSupplier(Map<String, Object> properties) {

        return () -> new GitInfoProvider() {

            @Override
            public Map<String, Object> getGitInfo() {
                return properties;
            }
        };
    }

    public Supplier<BuildInfoProvider> buildInfoSupplier(Map<String, Object> properties) {

        return () -> new BuildInfoProvider() {

            @Override
            public Map<String, Object> getBuildInfo() {
                return properties;
            }
        };
    }
}
