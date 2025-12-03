package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TenantDataService {

    private final Map<String, List<String>> tenantData = new ConcurrentHashMap<>();

    public TenantDataService() {
        // Seed some demo data
        tenantData.put("acme", new ArrayList<>(Arrays.asList(
                "ACME Rocket Launcher v2.0",
                "Instant Hole (Portable)",
                "Earthquake Pills")));
        tenantData.put("techstart", new ArrayList<>(Arrays.asList(
                "Cloud Native Platform",
                "AI-Powered Analytics",
                "DevOps Automation Suite")));
        tenantData.put("quantum", new ArrayList<>(Arrays.asList(
                "Quantum Processor Q-100",
                "Qubit Stabilizer",
                "Entanglement Detector")));
    }

    public List<String> getData(String tenantId) {
        return tenantData.getOrDefault(tenantId,
                Collections.singletonList("No data for tenant: " + tenantId));
    }

    public void addData(String tenantId, String item) {
        tenantData.computeIfAbsent(tenantId, k -> new ArrayList<>()).add(item);
    }
}