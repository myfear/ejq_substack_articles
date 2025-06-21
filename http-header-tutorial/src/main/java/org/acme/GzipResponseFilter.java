package org.acme;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

import io.quarkus.logging.Log;

@Provider
public class GzipResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        Log.infof("Processing GZIP Request");
        String acceptEncoding = requestContext.getHeaderString("Accept-Encoding");
        if (acceptEncoding != null && acceptEncoding.contains("gzip")) {
            // Check if the response already has a Content-Encoding header
            if (responseContext.getHeaders().containsKey("Content-Encoding")) {
                return;
            }

            // You could add more sophisticated logic here to check the content type, etc.
            if (responseContext.hasEntity()) {
                responseContext.getHeaders().putSingle("Content-Encoding", "gzip");
                responseContext.setEntityStream(new GZIPOutputStreamWrapper(responseContext.getEntityStream()));
            }
        }
    }
}