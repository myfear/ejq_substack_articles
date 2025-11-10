package com.example;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ConsistentRouter {

    private final TreeMap<Long, String> ring = new TreeMap<>();
    private final int replicas = 150;

    public void addServer(String name) {
        for (int i = 0; i < replicas; i++) {
            long hash = hash(name + i);
            ring.put(hash, name);
        }
    }

    public String route(String key) {
        if (ring.isEmpty())
            return null;
        long hash = hash(key);
        Map.Entry<Long, String> e = ring.ceilingEntry(hash);
        return e != null ? e.getValue() : ring.firstEntry().getValue();
    }

    private long hash(String s) {
        byte[] bytes = DigestUtils.md5(s);
        long h = 0;
        for (int i = 0; i < 8; i++)
            h = (h << 8) | (bytes[i] & 0xFF);
        return h;
    }
}