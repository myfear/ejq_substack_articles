package org.acme.plugin;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.inject.Inject;

@ApplicationScoped
public class PluginService {

    @Inject
    @Any
    Instance<Plugin> plugins;

    public Plugin byName(String name) {
        return plugins.select(NamedLiteral.of(name)).get();
    }
}