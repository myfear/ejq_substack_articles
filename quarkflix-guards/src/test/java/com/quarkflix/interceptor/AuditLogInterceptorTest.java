package com.quarkflix.interceptor;

import com.quarkflix.model.User;
import com.quarkflix.service.StreamingService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AuditLogInterceptorTest {

    @Inject
    StreamingService streamingService;

    @BeforeEach
    void setUp() {
        // Reset the test-specific static variable before each test
        AuditLogInterceptor.lastLoggedMessage_test_only = null;
    }

    @Test
    void testPlayMovieIsAudited() {
        User user = new User("testUser", 30, false);
        String movieId = "movie123";

        String result = streamingService.playMovie(user, movieId);
        assertNotNull(result, "Service method should return a result.");
        assertEquals(String.format("User %s is now playing movie %s.", user.getUsername(), movieId), result);

        assertNotNull(AuditLogInterceptor.lastLoggedMessage_test_only, "Audit log message should have been captured.");
        assertTrue(AuditLogInterceptor.lastLoggedMessage_test_only.contains("AUDIT"));
        assertTrue(AuditLogInterceptor.lastLoggedMessage_test_only.contains("params [" + user.toString() + ", " + movieId + "]"), "Log message parameters are incorrect.");
    }

    @Test
    void testGetAccountDetailsIsAudited() {
        User user = new User("accountUser", 40, true);
        streamingService.getAccountDetails(user);

        assertNotNull(AuditLogInterceptor.lastLoggedMessage_test_only);
        assertTrue(AuditLogInterceptor.lastLoggedMessage_test_only.contains("Account Access"));
    }

    @Test
    void testBrowseCatalogIsNotAudited() {
        User user = new User("browseUser", 20, false);
        streamingService.browseCatalog(user); // This method is not annotated with @AuditLog
        assertNull(AuditLogInterceptor.lastLoggedMessage_test_only, "Browse catalog should not trigger audit log interceptor.");
    }
}