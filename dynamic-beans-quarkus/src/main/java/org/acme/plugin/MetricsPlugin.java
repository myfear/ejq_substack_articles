package org.acme.plugin;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("metrics")
public class MetricsPlugin implements Plugin {
    @Override
    public String name() {
        return "metrics";
    }

    @Override
    public String apply(String input) {
        // pretend to record a metric
        return "metrics(" + input + ")";
    }
}