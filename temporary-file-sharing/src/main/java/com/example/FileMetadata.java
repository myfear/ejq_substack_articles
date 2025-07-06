package com.example;

import java.util.concurrent.atomic.AtomicInteger;

public record FileMetadata(
        String originalName,
        String objectName,
        int allowedDownloads,
        AtomicInteger counter) {

    boolean stillDownloadable() {
        return counter.get() < allowedDownloads;
    }

    void markServed() {
        counter.incrementAndGet();
    }
}
