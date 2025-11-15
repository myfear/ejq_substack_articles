package com.example.ai.cache;

import java.lang.reflect.Proxy;

import com.example.ai.cache.proxy.SemanticCacheInvocationHandler;
import com.example.ai.cache.store.SemanticCacheStore;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CachedAiServiceFactory {

    @Inject
    SemanticCacheStore cacheStore;

    /**
     * Wrap an AI service with semantic caching
     */
    @SuppressWarnings("unchecked")
    public <T> T createCached(T aiService, SemanticCacheConfig config) {
        Class<?> serviceClass = aiService.getClass();
        Class<?>[] interfaces = getAllInterfaces(serviceClass);

        if (interfaces.length == 0) {
            throw new IllegalArgumentException(
                    "AI service must implement at least one interface");
        }

        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                interfaces,
                new SemanticCacheInvocationHandler(aiService, cacheStore, config));
    }

    /**
     * Create with default configuration
     */
    public <T> T createCached(T aiService) {
        return createCached(aiService, SemanticCacheConfig.builder().build());
    }

    private Class<?>[] getAllInterfaces(Class<?> clazz) {
        java.util.Set<Class<?>> interfaces = new java.util.HashSet<>();
        collectInterfaces(clazz, interfaces);
        return interfaces.toArray(new Class<?>[0]);
    }

    private void collectInterfaces(Class<?> clazz, java.util.Set<Class<?>> interfaces) {
        if (clazz == null || clazz == Object.class) {
            return;
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            interfaces.add(iface);
            collectInterfaces(iface, interfaces);
        }

        collectInterfaces(clazz.getSuperclass(), interfaces);
    }

    /**
     * Get access to the cache store for management operations
     */
    public SemanticCacheStore getCacheStore() {
        return cacheStore;
    }

    /**
     * Get cache statistics
     */
    public CacheStatistics getStatistics() {
        return cacheStore.getStatistics();
    }

}