package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Path("/resource")
public class ResourceReaderResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response readResource() {
        String resourcePath = "/my-data.txt"; // Absolute path from classpath root

        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Resource not found: " + resourcePath)
                        .build();
            }

            String content = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            return Response.ok(content).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError()
                    .entity("Error reading resource: " + e.getMessage())
                    .build();
        }
    }

    // Alternative Thread Context ClassLoader for advanced use-cases  (often equivalent in Quarkus)
    private InputStream getResourceViaContextClassLoader(String path) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path.startsWith("/") ? path.substring(1) : path);
    }
}