package com.pixel.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.pixel.filter.ImageFilter;
import com.pixel.filter.impl.DownsampleFilter;
import com.pixel.filter.impl.FloydSteinbergFilter;
import com.pixel.filter.impl.UpsampleFilter;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FilterFactory {

    public ImageFilter createFilter(String type, Map<String, Object> params) {
        return switch (type.toLowerCase()) {
            case "downsample" -> new DownsampleFilter(
                    (Integer) params.getOrDefault("blockSize", 8));
            case "dither" -> new FloydSteinbergFilter();
            case "upsample" -> new UpsampleFilter(
                    (Integer) params.getOrDefault("scale", 8));
            default -> throw new IllegalArgumentException("Unknown filter: " + type);
        };
    }

    public List<ImageFilter> createChain(List<FilterConfigDTO> configs) {
        return configs.stream()
                .map(c -> createFilter(c.type(), c.params()))
                .collect(Collectors.toList());
    }
}