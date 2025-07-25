package org.acme;

import java.io.PrintWriter;
import java.io.StringWriter;

import io.quarkus.qute.Template;
import io.quarkus.runtime.LaunchMode;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class DevelopmentExceptionFilter implements ExceptionMapper<Exception> {

    @Inject
    AiErrorAnalyzer analyzer;

    @Inject
    Template errorAnalysis;

    @Override
    public Response toResponse(Exception exception) {
        if (LaunchMode.current() != LaunchMode.DEVELOPMENT) {
            // In non-development mode, return a generic error response
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Internal Server Error")
                    .build();
        }

        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        String analysis = analyzer.analyze(stackTrace);

        try {
            String html = errorAnalysis.data("analysis", analysis)
                    .data("stackTrace", stackTrace)
                    .render();
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(html)
                    .type("text/html; charset=UTF-8")
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing exception: " + e.getMessage())
                    .build();
        }
    }
}