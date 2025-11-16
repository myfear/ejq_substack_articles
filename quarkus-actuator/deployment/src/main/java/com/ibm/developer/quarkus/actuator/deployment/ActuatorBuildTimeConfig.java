package com.ibm.developer.quarkus.actuator.deployment;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
@ConfigMapping(prefix = "actuator")
public interface ActuatorBuildTimeConfig {
    /**
     * Enable or disable the actuator.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Enable or disable the info endpoint.
     */
    @WithDefault("true")
    boolean infoEnabled();

    /**
     * Base path for actuator endpoints.
     */
    @WithDefault("/actuator")
    String basePath();
}
