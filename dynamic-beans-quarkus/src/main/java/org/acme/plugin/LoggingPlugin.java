package org.acme.plugin;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("logging")
public class LoggingPlugin implements Plugin {
    @Override
    public String name() {
        return "logging";
    }

    @Override
    public String apply(String input) {
        Log.info("[PLUGIN:logging] " + input);
        return input;
    }
}