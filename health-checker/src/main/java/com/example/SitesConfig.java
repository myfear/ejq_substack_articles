package com.example;

import java.util.List;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "monitoring")
public interface SitesConfig {
    List<String> sites();
}