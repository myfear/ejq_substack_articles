package com.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContentCache {
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    private String key(String base, Object... parts) {
        StringBuilder sb = new StringBuilder(base);
        for (Object part : parts)
            sb.append("|").append(part);
        return DigestUtils.sha256Hex(sb.toString());
    }

    public void put(String base, String value, Object... parts) {
        cache.put(key(base, parts), value);
    }

    public String get(String base, Object... parts) {
        return cache.get(key(base, parts));
    }
}