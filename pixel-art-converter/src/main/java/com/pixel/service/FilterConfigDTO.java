package com.pixel.service;

import java.util.Map;

public record FilterConfigDTO(String type, Map<String, Object> params) {
}