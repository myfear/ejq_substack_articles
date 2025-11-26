package com.ibm.developer.quarkus.actuator.runtime;

import java.util.Map;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

import com.ibm.developer.quarkus.actuator.runtime.infoprovider.BuildInfoProvider;
import com.ibm.developer.quarkus.actuator.runtime.infoprovider.GitInfoProvider;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.runtime.annotations.RuntimeInit;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class ActuatorRecorder {

    private static final Logger log = Logger.getLogger(ActuatorRecorder.class);
    private final RuntimeValue<ActuatorRuntimeConfig> config;

    public ActuatorRecorder(RuntimeValue<ActuatorRuntimeConfig> config) {
        this.config = config;
    }

    @RuntimeInit
    public void init() {
        log.debugf("Actuator initialized at %s", config.getValue().basePath());
    }

    public Handler<RoutingContext> createInfoHandler() {
        return new ActuatorInfoEndpoint(config.getValue());
    }

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