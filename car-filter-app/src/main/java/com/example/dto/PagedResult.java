package com.example.dto;

import java.util.List;

public record PagedResult<T>(List<T> list, long totalCount) {
}