package com.ibm.developer.identicons;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/identicons")
public class QuantumIdenticonResource {

    QuantumCircuitGenerator generator = new QuantumCircuitGenerator();
    CircuitRenderer renderer = new CircuitRenderer();

    @GET
    @Path("/{username}")
    @Produces("image/png")
    public Response generateIdenticon(@PathParam("username") String username) throws IOException {
        Circuit circuit = generator.generate(username);
        BufferedImage image = renderer.render(circuit);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return Response.ok(baos.toByteArray()).build();
    }

    @GET
    @Path("/{username}/preview")
    @Produces(MediaType.APPLICATION_JSON)
    public Circuit getCircuitInfo(@PathParam("username") String username) {
        return generator.generate(username);
    }
}
