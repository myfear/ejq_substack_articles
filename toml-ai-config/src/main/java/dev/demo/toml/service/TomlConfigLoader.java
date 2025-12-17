package dev.demo.toml.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.ConfigProvider;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;

import dev.demo.toml.config.AiConfiguration;
import io.smallrye.config.SmallRyeConfig;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TomlConfigLoader {

    private static final Logger LOG = Logger.getLogger(TomlConfigLoader.class.getName());

    private final TomlMapper tomlMapper = new TomlMapper();
    private AiConfiguration configuration;
    private String loadedResourceName;

    @PostConstruct
    void init() {
        reload();
    }

    public synchronized void reload() {
        List<String> profiles = ConfigProvider.getConfig().unwrap(SmallRyeConfig.class).getProfiles();

        String activeProfile = Optional.ofNullable(profiles)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .orElse(null);

        ResourceInfo resourceInfo = resolveConfigResource(activeProfile);
        loadedResourceName = resourceInfo.name();

        try (InputStream inputStream = resourceInfo.inputStream()) {
            configuration = tomlMapper.readValue(inputStream, AiConfiguration.class);
            LOG.info(() -> "Loaded TOML config from " + loadedResourceName + " (title=" + configuration.getTitle()
                    + ", env=" + configuration.getEnvironment() + ")");
        } catch (IOException e) {
            throw new ConfigurationLoadException("Failed to load TOML configuration from " + loadedResourceName, e);
        }
    }

    /**
     * Resolves the configuration resource based on the active profile.
     * Tries profile-specific file first, then falls back to default.
     *
     * @param activeProfile the active profile name, or null if no profile is active
     * @return ResourceInfo containing the input stream and resource name
     * @throws ConfigurationLoadException if no configuration file is found
     */
    private ResourceInfo resolveConfigResource(String activeProfile) {
        String defaultFile = "ai-config.toml";

        String profileFile = Optional.ofNullable(activeProfile)
                .map(profile -> "ai-config-" + profile + ".toml")
                .orElse(null);

        // Try profile-specific file first
        if (profileFile != null) {
            InputStream profileStream = resource(profileFile);
            if (profileStream != null) {
                return new ResourceInfo(profileStream, profileFile);
            }
        }

        // Fall back to default file
        InputStream defaultStream = resource(defaultFile);
        if (defaultStream != null) {
            return new ResourceInfo(defaultStream, defaultFile);
        }

        // No configuration found
        String checkedFiles = buildCheckedFilesMessage(profileFile, defaultFile);
        throw new ConfigurationLoadException("No ai-config TOML found (checked " + checkedFiles + ")");
    }

    /**
     * Builds an error message listing which configuration files were checked.
     *
     * @param profileFile the profile-specific file name, or null
     * @param defaultFile the default file name
     * @return formatted message listing checked files
     */
    private String buildCheckedFilesMessage(String profileFile, String defaultFile) {
        return (profileFile != null)
                ? profileFile + " and " + defaultFile
                : defaultFile;
    }

    /**
     * Record holding information about a resolved configuration resource.
     *
     * @param inputStream the input stream for reading the resource
     * @param name        the name of the resource file
     */
    private record ResourceInfo(InputStream inputStream, String name) {
    }

    private InputStream resource(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    public AiConfiguration getConfiguration() {
        return configuration;
    }

    public String getLoadedResourceName() {
        return loadedResourceName;
    }
}