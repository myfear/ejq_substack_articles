package com.example.ai;

import java.time.Duration;

import com.example.ai.cache.CacheStatistics;
import com.example.ai.cache.CachedAiServiceFactory;
import com.example.ai.cache.SemanticCacheConfig;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/support")
@Produces(MediaType.APPLICATION_JSON)
public class CustomerSupportResource {

    @Inject
    CustomerSupportAgent rawAgent;

    @Inject
    CachedAiServiceFactory cacheFactory;

    private CustomerSupportAgent cachedAgent;

    void onStart(@Observes StartupEvent event) {
        // Create cached wrapper on startup
        SemanticCacheConfig config = SemanticCacheConfig.builder()
                .enabled(true)
                .similarityThreshold(0.90)
                .ttl(Duration.ofMinutes(30))
                .strategy(SemanticCacheConfig.CacheStrategy.HYBRID)
                .embeddingModelName("default")
                .build();

        this.cachedAgent = cacheFactory.createCached(rawAgent, config);
    }

    @POST
    @Path("/chat")
    public String chat(String message) {
        return cachedAgent.chat(message);
    }

    @POST
    @Path("/analyze")
    public String analyze(String content) {
        return cachedAgent.analyze(content);
    }

    @Path("/cache/stats")
    @GET
    public CacheStatistics.StatisticsSnapshot getStats() {
        return cacheFactory.getStatistics().getSnapshot();
    }

    @DELETE
    @Path("/cache/clear")
    public void clearCache() {
        cacheFactory.getCacheStore().clear();
    }

}