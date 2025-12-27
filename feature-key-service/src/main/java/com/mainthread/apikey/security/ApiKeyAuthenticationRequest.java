package com.mainthread.apikey.security;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.security.identity.request.AuthenticationRequest;

public class ApiKeyAuthenticationRequest implements AuthenticationRequest {

    private final String rawApiKey;
    private final Map<String, Object> attributes = new HashMap<>();

    public ApiKeyAuthenticationRequest(String rawApiKey) {
        this.rawApiKey = rawApiKey;
    }

    public String rawApiKey() {
        return rawApiKey;
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
}