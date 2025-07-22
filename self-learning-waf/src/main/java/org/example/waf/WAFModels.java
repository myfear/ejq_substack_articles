package org.example.waf;

import java.util.Map;

enum WAFAction {
    ALLOW, BLOCK, RATE_LIMIT, LOG_ONLY
}

record RestRequestInfo(
        String method,
        String path,
        String query,
        String clientIP,
        long timestamp,
        Map<String, String> headers,
        String contentType,
        int contentLength) {
}

record WAFDecision(
        boolean isAnomaly,
        WAFAction action,
        double confidence,
        double reconstructionError,
        String reason) {
}

record WAFLogEntry(
        long timestamp,
        String clientIP,
        String method,
        String path,
        WAFAction action,
        double reconstructionError,
        long processingTime,
        String reason) {
}