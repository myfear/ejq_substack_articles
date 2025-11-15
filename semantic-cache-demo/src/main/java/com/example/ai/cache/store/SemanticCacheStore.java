package com.example.ai.cache.store;

import java.util.Optional;

import com.example.ai.cache.CacheStatistics;
import com.example.ai.cache.CachedResponse;
import com.example.ai.cache.SemanticCacheConfig;

public interface SemanticCacheStore {

        /**
         * Look up a cached response based on semantic similarity
         */
        Optional<CachedResponse> lookup(
                        String prompt,
                        String llmString,
                        SemanticCacheConfig config);

        /**
         * Store a response in the cache
         */
        void store(
                        String prompt,
                        String llmString,
                        Object response,
                        SemanticCacheConfig config);

        /**
         * Clear all cache entries
         */
        void clear();

        /**
         * Clear expired entries
         */
        void evictExpired();

        /**
         * Get cache statistics
         */
        CacheStatistics getStatistics();
}
