package org.acme;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource for reading files from the classpath.
 * <p>
 * This resource provides an endpoint to read and return the contents of
 * resource files located in the classpath, such as files in the resources directory.
 * </p>
 *
 * @author Generated
 */
@Path("/resource")
public class ResourceReaderResource {

    /**
     * Reads a resource file from the classpath and returns its contents as plain text.
     * <p>
     * This method reads the file "/my-data.txt" from the classpath root and returns
     * its contents. The file is read using UTF-8 encoding.
     * </p>
     *
     * @return a {@link Response} containing:
     *         <ul>
     *         <li>HTTP 200 (OK) with the file contents if the resource is found</li>
     *         <li>HTTP 404 (NOT FOUND) if the resource file does not exist</li>
     *         <li>HTTP 500 (INTERNAL SERVER ERROR) if an error occurs while reading the file</li>
     *         </ul>
     */
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

    /**
     * Alternative method to load a resource using the Thread Context ClassLoader.
     * <p>
     * This method provides an alternative approach to resource loading that uses
     * the context classloader of the current thread. In Quarkus, this is often
     * equivalent to using {@code getClass().getResourceAsStream()}, but may be
     * useful in certain advanced use-cases or when dealing with complex classloader
     * hierarchies.
     * </p>
     *
     * @param path the resource path. If the path starts with "/", it is treated as
     *             an absolute path from the classpath root; otherwise, it is treated
     *             as a relative path. Note that when using the context classloader,
     *             leading slashes are removed.
     * @return an {@link InputStream} for reading the resource, or {@code null} if
     *         the resource is not found
     */
    private InputStream getResourceViaContextClassLoader(String path) {
        return Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path.startsWith("/") ? path.substring(1) : path);
    }
}