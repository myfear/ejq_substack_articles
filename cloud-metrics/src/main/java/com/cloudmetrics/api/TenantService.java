package com.cloudmetrics.api;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class TenantService {
  private static final Map<String, String> PLANS = Map.of(
    "tenant-free-user", "FREE",
    "tenant-pro-user",  "PRO"
  );

  public Optional<String> getPlan(String tenantId) {
    return Optional.ofNullable(PLANS.get(tenantId));
  }
}
