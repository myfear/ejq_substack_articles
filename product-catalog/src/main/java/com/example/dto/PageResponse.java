package com.example.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        String nextCursor,
        boolean hasMore,
        long count) {
}
