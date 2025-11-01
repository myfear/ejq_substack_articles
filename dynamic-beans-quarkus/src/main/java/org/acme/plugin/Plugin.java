package org.acme.plugin;

public interface Plugin {
    String name();

    String apply(String input);
}