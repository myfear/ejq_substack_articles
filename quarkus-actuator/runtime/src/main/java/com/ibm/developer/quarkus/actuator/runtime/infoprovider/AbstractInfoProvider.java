package com.ibm.developer.quarkus.actuator.runtime.infoprovider;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jboss.logging.Logger;

import com.ibm.developer.quarkus.actuator.runtime.ActuatorRuntimeConfig;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public abstract class AbstractInfoProvider {

    protected final Logger log = Logger.getLogger(getClass());

    @Inject
    protected ActuatorRuntimeConfig config;

    /**
     * Creates a new LinkedHashMap instance for maintaining insertion order.
     * 
     * @return a new LinkedHashMap instance
     */
    protected Map<String, Object> newMap() {
        return new LinkedHashMap<>();
    }
}

