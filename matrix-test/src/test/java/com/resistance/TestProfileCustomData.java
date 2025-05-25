package com.resistance;

import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class TestProfileCustomData implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        
        Map<String, String> config = new java.util.HashMap<>();
        config.put("quarkus.flyway.migrate-at-start", "false");
        config.put("quarkus.hibernate-orm.database.generation", "drop-and-create");
        return config;
    }
}