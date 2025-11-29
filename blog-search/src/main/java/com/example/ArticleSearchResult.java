package com.example;

public record ArticleSearchResult(Long id, String title, String url, String snippet, float rank, int matchPercentage) {
}