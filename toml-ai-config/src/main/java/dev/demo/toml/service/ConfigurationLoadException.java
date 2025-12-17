package dev.demo.toml.service;

/**
 * Exception thrown when TOML configuration cannot be loaded.
 * This is an unchecked exception that wraps underlying I/O or parsing errors.
 */
public class ConfigurationLoadException extends RuntimeException {

    public ConfigurationLoadException(String message) {
        super(message);
    }

    public ConfigurationLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}