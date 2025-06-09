package com.cloudmetrics.api;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucketBuilder;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@DynamicRateLimited
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_BEFORE + 200)
public class DynamicRateLimitInterceptor {


  private static final Logger LOG = Logger.getLogger(DynamicRateLimitInterceptor.class);

    @Inject
    TenantService tenantService;
    @Inject
    TenantContext tenantContext;

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static Bandwidth[] configFor(String plan) {
        if ("PRO".equals(plan)) {
            return new Bandwidth[] {
                    Bandwidth.builder()
                            .capacity(10)
                            .refillIntervally(10, Duration.ofMinutes(1))
                            .build(),
                    Bandwidth.builder()
                            .capacity(3)
                            .refillIntervally(3, Duration.ofSeconds(5))
                            .build()
            };
        } else {
            return new Bandwidth[] {
                    Bandwidth.builder()
                            .capacity(2)
                            .refillIntervally(2, Duration.ofMinutes(1))
                            .build()
            };
        }
    }

    private Bucket resolveBucket(String tenantId) {
        return buckets.computeIfAbsent(tenantId, key -> {
            String plan = tenantService.getPlan(key).orElse("FREE");
            Bandwidth[] limits = configFor(plan);
            LocalBucketBuilder bucket = Bucket.builder();
            for (Bandwidth limit : limits) {
                bucket.addLimit(limit);
            }
            return bucket.build();
        });
    }

    @AroundInvoke
    public Object around(InvocationContext ctx) throws Exception {
        String tenantId = tenantContext.getTenantId();
        LOG.infof("Tenant ID: %s", tenantId);
        if (tenantId == null || tenantId.isBlank()) {
            throw new WebApplicationException("Missing tenant", Response.Status.FORBIDDEN);
        }

        Bucket bucket = resolveBucket(tenantId);
        if (bucket.tryConsume(1)) {
            return ctx.proceed();
        }

        throw new WebApplicationException("Too Many Requests", Response.Status.TOO_MANY_REQUESTS);
    }
}
