package org.acme.api;

import io.quarkiverse.bucket4j.runtime.RateLimitException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RateLimitExceptionMapper implements ExceptionMapper<RateLimitException> {
    @Override
    public Response toResponse(RateLimitException ex) {
        // A conservative 1-second retry hint; adapt with ex info if you expose it
        return Response.status(429)
                .header("Retry-After", "1")
                .entity("{\"error\":\"Too Many Requests\"}")
                .build();
    }
}