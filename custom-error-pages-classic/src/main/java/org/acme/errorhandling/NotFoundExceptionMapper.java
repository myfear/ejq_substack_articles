package org.acme.errorhandling;

import java.util.List;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Inject
    @Location("errors/404.html")
    Template notFoundPage;

    @Context
    UriInfo uriInfo;
    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(NotFoundException exception) {
        String path = uriInfo.getPath();
       
        List<MediaType> acceptableMediaTypes = headers.getAcceptableMediaTypes();
        boolean onlyJsonIsAcceptable = false;

        if (acceptableMediaTypes.size() == 1) {
            // If the list has exactly one item, check if it's compatible with JSON
            onlyJsonIsAcceptable = MediaType.APPLICATION_JSON_TYPE.isCompatible(acceptableMediaTypes.get(0));
        }

        boolean prefersJson = onlyJsonIsAcceptable || uriInfo.getPath().startsWith("/api/");


        if (prefersJson) {
            return Response.status(404)
                    .entity(new GenericExceptionMapper.ErrorResponse(
                            null, "Resource not found at path: " + path, 404, null, exception.getMessage()))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.status(404)
                .entity(notFoundPage.data("path", path))
                .type(MediaType.TEXT_HTML)
                .build();
    }
}