package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DeduplicationService {

    public Map<String, List<String>> findDuplicates(File directory) throws IOException {
        Map<String, List<String>> map = new HashMap<>();
        scan(directory, map);
        map.entrySet().removeIf(e -> e.getValue().size() < 2);
        return map;
    }

    private void scan(File dir, Map<String, List<String>> map) throws IOException {
        File[] files = dir.listFiles();
        if (files == null)
            return;
        for (File f : files) {
            if (f.isDirectory())
                scan(f, map);
            else {
                try (InputStream is = new FileInputStream(f)) {
                    String hash = DigestUtils.sha256Hex(is);
                    map.computeIfAbsent(hash, k -> new ArrayList<>()).add(f.getAbsolutePath());
                }
            }
        }
    }
}