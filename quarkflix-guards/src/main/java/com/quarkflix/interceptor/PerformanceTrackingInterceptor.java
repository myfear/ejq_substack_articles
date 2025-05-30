package com.quarkflix.interceptor;

import org.jboss.logging.Logger;

import com.quarkflix.annotation.TrackPerformance;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@TrackPerformance
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 150) // Between AgeVerifier (100) and AuditLog (200)
public class PerformanceTrackingInterceptor {

    private static final Logger LOG = Logger.getLogger(PerformanceTrackingInterceptor.class);
    public static long lastExecutionTime_test_only = -1;

    @AroundInvoke
    Object track(InvocationContext context) throws Exception {
        long startTime = System.nanoTime();
        try {
            return context.proceed();
        } finally {
            long endTime = System.nanoTime();
            long durationNanos = endTime - startTime;
            long durationMillis = durationNanos / 1_000_000;
            lastExecutionTime_test_only = durationMillis; // For testing

            LOG.infof("PERF_TRACK: Method %s.%s executed in %d ms (%d ns).",
                    context.getTarget().getClass().getSimpleName(),
                    context.getMethod().getName(),
                    durationMillis,
                    durationNanos);
        }
    }
}