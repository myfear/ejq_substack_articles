package com.quarkflix.interceptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.quarkflix.model.User;
import com.quarkflix.service.StreamingService;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class PerformanceTrackingInterceptorTest {

    @Inject
    StreamingService streamingService;

    @BeforeEach
    void setUp() {
        AuditLogInterceptor.lastLoggedMessage_test_only = null;
        PerformanceTrackingInterceptor.lastExecutionTime_test_only = -1;
    }

    @Test
    void testContinueWatchingListPerformanceIsTrackedAndAudited() {
        User user = new User("perfUser", 30, false);

        String result = streamingService.getContinueWatchingList(user);

        assertNotNull(result);
        assertTrue(result.startsWith("Continue Watching List for perfUser"));

        // Check Performance Tracking
        assertTrue(PerformanceTrackingInterceptor.lastExecutionTime_test_only >= 50,
                "Execution time should be recorded and be at least 50ms.");
        assertTrue(PerformanceTrackingInterceptor.lastExecutionTime_test_only < 200,
                "Execution time should be reasonable (sanity check)."); // Adjusted if Thread.sleep is longer

        // Check Audit Log (it should still run)
        // With Performance (150) and Audit (200), Performance is outer.
        // AuditLogInterceptor should still be invoked.
        assertNotNull(AuditLogInterceptor.lastLoggedMessage_test_only, "Audit log should not be null.");
        assertTrue(AuditLogInterceptor.lastLoggedMessage_test_only
                .contains("AUDIT"));

        // To confirm order, you would need to capture multiple log lines and check
        // their sequence.
        // For this test, we primarily confirm both interceptors ran and did their job.
    }
}