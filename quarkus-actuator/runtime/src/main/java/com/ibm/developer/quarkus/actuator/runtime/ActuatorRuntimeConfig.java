package com.ibm.developer.quarkus.actuator.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "actuator")
public interface ActuatorRuntimeConfig {

    /**
     * Base path for actuator endpoints.
     */
    @WithDefault("/actuator")
    String basePath();

    /**
     * Info configuration.
     */
    InfoConfig info();

    interface InfoConfig {
        /**
         * Enable git information.
         */
        @WithDefault("true")
        boolean gitEnabled();

        /**
         * Enable Java information.
         */
        @WithDefault("true")
        boolean javaEnabled();

        /**
         * Enable SSL information.
         */
        @WithDefault("false")
        boolean sslEnabled();

        /**
         * Git information configuration.
         */
        GitConfig git();

        interface GitConfig {
            /**
             * Git branch name.
             */
            Optional<String> branch();

            /**
             * Git commit ID.
             */
            Optional<String> commitId();

            /**
             * Git commit time.
             */
            Optional<String> commitTime();
        }
    }
}