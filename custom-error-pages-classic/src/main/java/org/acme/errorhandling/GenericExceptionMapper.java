package org.acme.errorhandling;

import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(GenericExceptionMapper.class);

    @Inject
    @Location("errors/500.html")
    Template error500Page;

    @Context
    UriInfo uriInfo;
    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(Throwable exception) {
        UUID errorId = UUID.randomUUID();
        
        LaunchMode.current();
        boolean devMode = LaunchMode.isDev();
        
        LOG.errorf(exception, "Unhandled exception (ID %s) at %s", errorId, uriInfo.getPath());

        List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
        boolean onlyJsonIsAcceptable = false;

        if (acceptableMediaTypes.size() == 1) {
            // If the list has exactly one item, check if it's compatible with JSON
            onlyJsonIsAcceptable = MediaType.APPLICATION_JSON_TYPE.isCompatible(acceptableMediaTypes.get(0));
        }

        boolean prefersJson = onlyJsonIsAcceptable || uriInfo.getPath().startsWith("/api/");

        if (prefersJson) {
            return Response.status(500)
                    .entity(new ErrorResponse(errorId.toString(), "An unexpected error occurred.", 500,
                            devMode ? exception.getClass().getName() : null,
                            devMode ? exception.getMessage() : null))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.status(500)
                .entity(error500Page
                        .data("reference", errorId.toString())
                        .data("exceptionType", exception.getClass().getName())
                        .data("exceptionMessage", exception.getMessage())
                        .data("devMode", devMode))
                .type(MediaType.TEXT_HTML)
                .build();
    }

    public static class ErrorResponse {
        public String errorId, message, exceptionType, exceptionMessage;
        public int statusCode;

        public ErrorResponse(String id, String msg, int code, String type, String detail) {
            this.errorId = id;
            this.message = msg;
            this.statusCode = code;
            this.exceptionType = type;
            this.exceptionMessage = detail;
        }
    }
}
