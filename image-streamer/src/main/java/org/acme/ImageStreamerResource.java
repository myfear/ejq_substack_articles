package org.acme;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Path("/api/image")
public class ImageStreamerResource {

    @GET
    @Path("/{name}")
    @Produces("image/png")
    public Multi<byte[]> streamImage(@PathParam("name") String name) {
        java.nio.file.Path path = java.nio.file.Path.of("src/main/resources/images", name);
        if (!Files.exists(path)) {
            throw new NotFoundException("Image not found: " + name);
        }

        try {
            InputStream inputStream = Files.newInputStream(path);
            byte[] buffer = new byte[4096];

            return Multi.createFrom().emitter(emitter -> {
                new Thread(() -> {
                    try (inputStream) {
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            byte[] chunk = new byte[bytesRead];
                            System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                            emitter.emit(chunk);
                            Thread.sleep(50); // simulate progressive generation
                        }
                        emitter.complete();
                    } catch (Exception e) {
                        emitter.fail(e);
                    }
                }).start();
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}