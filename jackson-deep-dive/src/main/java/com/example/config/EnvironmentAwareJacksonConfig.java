package com.example.config;

import com.fasterxml.jackson.databind.*;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
public class EnvironmentAwareJacksonConfig implements ObjectMapperCustomizer {

    @ConfigProperty(name = "quarkus.profile")
    String profile;

    @Override
    public void customize(ObjectMapper mapper) {
        if ("dev".equals(profile)) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        } else {
            mapper.disable(SerializationFeature.INDENT_OUTPUT);
        }
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }
}